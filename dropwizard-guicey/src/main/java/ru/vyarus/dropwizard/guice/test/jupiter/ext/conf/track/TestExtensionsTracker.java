package ru.vyarus.dropwizard.guice.test.jupiter.ext.conf.track;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import ru.vyarus.dropwizard.guice.debug.util.RenderUtils;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.test.EnableHook;
import ru.vyarus.dropwizard.guice.test.jupiter.env.EnableSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.field.AnnotatedField;
import ru.vyarus.dropwizard.guice.test.util.ConfigModifier;
import ru.vyarus.dropwizard.guice.test.util.PrintUtils;
import ru.vyarus.dropwizard.guice.test.util.TestSetupUtils;

import java.lang.annotation.Annotation;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tracks registration of hooks and support objects during test initialization in order to log used
 * additions (to simplify applied objects tracking). Also, tracks applied configuration overrides, but only after
 * application start (the only way to show actually applied values).
 * <p>
 * Also, tracks guicey extensions performance to simplify test performance problems resolution.
 *
 * @author Vyacheslav Rusakov
 * @since 27.05.2022
 */
@SuppressWarnings({"PMD.GodClass", "PMD.TooManyMethods"})
public class TestExtensionsTracker {

    /**
     * System property enables debug output for all used guicey extensions.
     */
    public static final String GUICEY_EXTENSIONS_DEBUG = "guicey.extensions.debug";
    /**
     * Enabled value for {@link #GUICEY_EXTENSIONS_DEBUG} system property.
     */
    public static final String DEBUG_ENABLED = "true";

    @SuppressWarnings("checkstyle:VisibilityModifier")
    public boolean debug;

    protected final List<String> extensionsSource = new ArrayList<>();
    protected final List<String> hooksSource = new ArrayList<>();
    protected final List<String> configModifierSource = new ArrayList<>();
    private final List<PerformanceTrack> performance = new ArrayList<>();

    private GuiceyTestTime testPhase;
    private Duration lastOverall;
    private Class<? extends TestEnvironmentSetup> contextSetupObject;
    // setup object in field could be a lambda, and it could register hook with lambda - need to remember field name
    // to provide meaningful registration context in the report
    private final Map<Class<?>, String> fieldSetupObjectsReference = new HashMap<>();

    public void setContextSetupObject(final Class<? extends TestEnvironmentSetup> setup) {
        contextSetupObject = setup;
    }

    @SuppressWarnings("unchecked")
    public final void extensionsFromFields(final List<AnnotatedField<EnableSetup, TestEnvironmentSetup>> fields,
                                           final Object instance) {
        final String prefix = "@" + EnableSetup.class.getSimpleName();
        RegistrationTrackUtils.fromField(extensionsSource, prefix,
                (List<AnnotatedField<?, ?>>) (List) fields, instance);
        // store meaningful names to clearly identify lambda hook registration source
        fields.forEach(field -> fieldSetupObjectsReference
                .put(field.getCachedValue().getClass(), prefix + " "
                        + RegistrationTrackUtils.getFieldDescriptor(field)));
    }

    @SafeVarargs
    public final void extensionsFromAnnotation(final Class<? extends Annotation> ann,
                                               final Class<? extends TestEnvironmentSetup>... exts) {
        // sync actual extension registration order with tracking info
        final List<String> tmp = new ArrayList<>(extensionsSource);
        extensionsSource.clear();
        RegistrationTrackUtils.fromClass(extensionsSource, "@" + ann.getSimpleName() + "(setup)", exts, true);
        extensionsSource.addAll(tmp);
    }

    @SuppressWarnings("unchecked")
    public final void hooksFromFields(final List<AnnotatedField<EnableHook, GuiceyConfigurationHook>> fields,
                                      final boolean baseHooks,
                                      final Object instance) {
        if (!fields.isEmpty()) {
            // hooks from fields in base classes activated before configured hooks
            final List<String> tmp = baseHooks ? new ArrayList<>(hooksSource) : Collections.emptyList();
            if (baseHooks) {
                hooksSource.clear();
            }
            RegistrationTrackUtils.fromField(hooksSource, "@" + EnableHook.class.getSimpleName(),
                    (List<AnnotatedField<?, ?>>) (List) fields, instance);
            hooksSource.addAll(tmp);
        }
    }

    @SafeVarargs
    public final void hooksFromAnnotation(final Class<? extends Annotation> ann,
                                          final Class<? extends GuiceyConfigurationHook>... exts) {
        RegistrationTrackUtils.fromClass(hooksSource, "@" + ann.getSimpleName() + "(hooks)", exts, true);
    }

    public final void extensionInstances(final TestEnvironmentSetup... exts) {
        RegistrationTrackUtils.fromInstance(extensionsSource, String.format("@%s.setup(obj)",
                RegisterExtension.class.getSimpleName()), exts);
    }

    @SafeVarargs
    public final void extensionClasses(final Class<? extends TestEnvironmentSetup>... exts) {
        RegistrationTrackUtils.fromClass(extensionsSource, String.format("@%s.setup(class)",
                RegisterExtension.class.getSimpleName()), exts, false);
    }

    @SafeVarargs
    public final List<TestEnvironmentSetup> lookupExtensions(final TestEnvironmentSetup... exts) {
        RegistrationTrackUtils.fromInstance(extensionsSource, "lookup (service loader)", exts);
        return Arrays.asList(exts);
    }

    @SafeVarargs
    public final List<TestEnvironmentSetup> defaultExtensions(final TestEnvironmentSetup... exts) {
        RegistrationTrackUtils.fromInstance(extensionsSource, "default extension", exts);
        return Arrays.asList(exts);
    }

    public final void hookInstances(final GuiceyConfigurationHook... exts) {
        RegistrationTrackUtils.fromInstance(hooksSource, String.format("%s.hooks(obj)", getHookContext()), exts);
    }

    @SafeVarargs
    public final void hookClasses(final Class<? extends GuiceyConfigurationHook>... exts) {
        RegistrationTrackUtils.fromClass(hooksSource, String.format("%s.hooks(class)", getHookContext()), exts, false);
    }

    @SafeVarargs
    public final void configModifiersFromAnnotation(final Class<? extends Annotation> ann,
                                                    final Class<? extends ConfigModifier>... exts) {
        RegistrationTrackUtils.fromClass(configModifierSource, "@" + ann.getSimpleName() + "(configModifiers)",
                exts, true);
    }

    @SafeVarargs
    public final void configModifierClasses(final Class<? extends ConfigModifier>... mods) {
        RegistrationTrackUtils.fromClass(configModifierSource, String.format("%s.configModifiers(class)",
                getHookContext()), mods, false);
    }

    public final void configModifierInstances(final ConfigModifier... exts) {
        RegistrationTrackUtils.fromInstance(configModifierSource, String.format("%s.configModifiers(obj)",
                getHookContext()), exts);
    }

    public void lifecyclePhase(final ExtensionContext context, final GuiceyTestTime phase) {
        testPhase = phase;
    }

    public void performanceTrack(final GuiceyTestTime name, final Duration duration) {
        performanceTrack(name, duration, false);
    }

    public void performanceTrack(final GuiceyTestTime name, final Duration duration, final boolean append) {
        PerformanceTrack track = performance.stream()
                .filter(tr -> tr.phase == testPhase && tr.name == name)
                .findFirst().orElse(null);
        if (track == null) {
            track = new PerformanceTrack(name, testPhase);
            performance.add(track);
        }
        track.registerDuration(duration, append);
    }

    /**
     * In some cases it might be simpler to use system property to enable debug: {@code -Dguicey.extensions.debug=true}.
     */
    @SuppressFBWarnings("PA_PUBLIC_PRIMITIVE_ATTRIBUTE")
    public void enableDebugFromSystemProperty() {
        if (!debug && DEBUG_ENABLED.equalsIgnoreCase(System.getProperty(GUICEY_EXTENSIONS_DEBUG))) {
            debug = true;
        }
    }

    /**
     * Logs registered setup objects and hooks. Do nothing if no setup objects or hooks registered.
     *
     * @param configPrefix configuration prefix
     */
    @SuppressWarnings("PMD.SystemPrintln")
    public void logUsedHooksAndSetupObjects(final String configPrefix) {
        if (debug && (!extensionsSource.isEmpty() || !hooksSource.isEmpty())) {
            // using config prefix to differentiate outputs for parallel execution
            final StringBuilder res = new StringBuilder(500).append("\nGuicey test extensions (")
                    .append(configPrefix).append(".):\n\n");
            if (!extensionsSource.isEmpty()) {
                res.append("\tSetup objects = \n");
                logTracks(res, extensionsSource);
            }

            if (!hooksSource.isEmpty()) {
                res.append("\tTest hooks = \n");
                logTracks(res, hooksSource);
            }
            System.out.println(res);
        }
    }

    /**
     * Logs overridden configurations. Show values already applied to system properties.
     *
     * @param configPrefix configuration prefix
     */
    @SuppressWarnings("PMD.SystemPrintln")
    public void logOverriddenConfigs(final String configPrefix) {
        if (debug) {
            final StringBuilder res = new StringBuilder(100);
            for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
                final String key = (String) entry.getKey();
                if (key.startsWith(configPrefix)) {
                    res.append(String.format("\t %20s = %s%n",
                            key.substring(configPrefix.length() + 1), entry.getValue()));
                }
            }

            final boolean hasOverrides = !res.isEmpty();

            if (!configModifierSource.isEmpty()) {
                res.append("\nConfiguration modifiers:\n");
                logTracks(res, configModifierSource);
            }

            System.out.println((hasOverrides ? "\nConfiguration overrides (" + configPrefix + ".):\n" : "")
                    + res);
        }
    }

    @SuppressWarnings("PMD.SystemPrintln")
    public void logGuiceyTestTime(final GuiceyTestTime phase, final ExtensionContext context) {
        if (debug) {
            final StringBuilder res = new StringBuilder();
            Duration overall = Duration.ZERO;
            for (PerformanceTrack root : performance) {
                if (!root.isRoot()) {
                    continue;
                }
                overall = overall.plus(root.getOverall());
                res.append("\n\t").append(root).append('\n');

                for (PerformanceTrack track : performance) {
                    if (track.isRoot() || track.phase != root.name) {
                        continue;
                    }
                    res.append("\t\t").append(track).append('\n');
                }
            }

            // merge increase delta to start tracking new increases
            performance.forEach(PerformanceTrack::applyIncrease);

            final String title = PrintUtils.getPerformanceReportSeparator(context)
                    + "Guicey time after [" + phase.getDisplayName() + "] of "
                    + TestSetupUtils.getContextTestName(context)
                    + ": " + PrintUtils.renderTime(overall, lastOverall == null ? null : overall.minus(lastOverall));
            lastOverall = overall;

            System.out.println(title + "\n" + res);
        }
    }

    private String getHookContext() {
        // hook might be registered from manual extension in filed or within setup object and in this case
        // tracking setup object class
        String res;
        if (contextSetupObject != null) {
            // special case: hook registartion under setup object from @EnableSetup field (both could be lambda)
            res = fieldSetupObjectsReference.get(contextSetupObject);
            if (res == null) {
                res = RenderUtils.getClassName(contextSetupObject);
            }
        } else {
            res = "@" + RegisterExtension.class.getSimpleName();
        }
        return res;
    }

    private void logTracks(final StringBuilder res, final List<String> tracks) {
        for (String st : tracks) {
            res.append("\t\t").append(st).append('\n');
        }
        res.append('\n');
    }

    /**
     * Depending on test, application could be instantiated before all or before each test methods.
     * When executing methods of the same test, indicating increased time (e.g., each beforeEach).
     */
    @SuppressWarnings("VisibilityModifier")
    private static class PerformanceTrack {
        final GuiceyTestTime name;
        final GuiceyTestTime phase;
        Duration duration;
        Duration increase;

        PerformanceTrack(final GuiceyTestTime name, final GuiceyTestTime phase) {
            this.name = name;
            this.phase = phase;
        }

        void registerDuration(final Duration duration, final boolean append) {
            if (this.duration == null) {
                this.duration = duration;
            } else {
                if (this.increase == null && append) {
                    // immediate append (for timers executed several times)
                    this.duration = this.duration.plus(duration);
                } else {
                    if (increase == null) {
                        this.increase = duration;
                    } else {
                        // possible when debug not enabled - apply not performed (stats just collected)
                        this.increase = this.increase.plus(duration);
                    }
                }
            }
        }

        void applyIncrease() {
            if (increase != null) {
                duration = duration.plus(increase);
                increase = null;
            }
        }

        boolean isRoot() {
            return phase == name;
        }

        Duration getOverall() {
            return increase == null ? duration : duration.plus(increase);
        }

        @Override
        public String toString() {
            String title = name.getDisplayName();
            if (isRoot()) {
                title = "[" + title + "]";
            }
            return String.format("%-35s: %s", title, PrintUtils.renderTime(getOverall(), increase));
        }
    }
}
