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
public class TestExtensionsTracker {

    /**
     * System property enables debug output for all used guicey extensions.
     */
    public static final String GUICEY_EXTENSIONS_DEBUG = "guicey.extensions.debug";
    /**
     * Enabled value for {@link #GUICEY_EXTENSIONS_DEBUG} system property.
     */
    public static final String DEBUG_ENABLED = "true";

    /**
     * Extension debug state.
     */
    @SuppressWarnings("checkstyle:VisibilityModifier")
    public boolean debug;

    /**
     * Setup objects registration sources.
     */
    protected final List<String> extensionsSource = new ArrayList<>();
    /**
     * Hooks registration sources.
     */
    protected final List<String> hooksSource = new ArrayList<>();
    /**
     * Configuration modifiers registration sources.
     */
    protected final List<String> configModifierSource = new ArrayList<>();
    private final List<PerformanceTrack> performance = new ArrayList<>();

    private GuiceyTestTime testPhase;
    private Class<? extends TestEnvironmentSetup> contextSetupObject;
    // setup object in field could be a lambda, and it could register hook with lambda - need to remember field name
    // to provide meaningful registration context in the report
    private final Map<Class<?>, String> fieldSetupObjectsReference = new HashMap<>();

    /**
     * @param setup context test extension
     */
    public void setContextSetupObject(final Class<? extends TestEnvironmentSetup> setup) {
        contextSetupObject = setup;
    }

    /**
     * Register setup objects from test fields.
     *
     * @param fields   fields
     * @param instance test instance
     */
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

    /**
     * Register setup objects from annotation.
     *
     * @param ann  annotation type
     * @param exts setup objects
     */
    @SafeVarargs
    public final void extensionsFromAnnotation(final Class<? extends Annotation> ann,
                                               final Class<? extends TestEnvironmentSetup>... exts) {
        // sync actual extension registration order with tracking info
        final List<String> tmp = new ArrayList<>(extensionsSource);
        extensionsSource.clear();
        RegistrationTrackUtils.fromClass(extensionsSource, "@" + ann.getSimpleName() + "(setup)", exts, true);
        extensionsSource.addAll(tmp);
    }

    /**
     * Register hooks from test fields.
     *
     * @param fields    fields
     * @param baseHooks hooks from base class
     * @param instance  test instance
     */
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

    /**
     * Register hooks from annotation.
     *
     * @param ann  annotation type
     * @param exts hooks
     */
    @SafeVarargs
    public final void hooksFromAnnotation(final Class<? extends Annotation> ann,
                                          final Class<? extends GuiceyConfigurationHook>... exts) {
        RegistrationTrackUtils.fromClass(hooksSource, "@" + ann.getSimpleName() + "(hooks)", exts, true);
    }

    /**
     * Register setup objects instances.
     *
     * @param exts setup objects
     */
    public final void extensionInstances(final TestEnvironmentSetup... exts) {
        RegistrationTrackUtils.fromInstance(extensionsSource, String.format("@%s.setup(obj)",
                RegisterExtension.class.getSimpleName()), exts);
    }

    /**
     * Register setup objects classes.
     *
     * @param exts setup object classes
     */
    @SafeVarargs
    public final void extensionClasses(final Class<? extends TestEnvironmentSetup>... exts) {
        RegistrationTrackUtils.fromClass(extensionsSource, String.format("@%s.setup(class)",
                RegisterExtension.class.getSimpleName()), exts, false);
    }

    /**
     * Register setup objects resolved with service loader.
     *
     * @param exts setup objects
     * @return setup object instances
     */
    @SafeVarargs
    public final List<TestEnvironmentSetup> lookupExtensions(final TestEnvironmentSetup... exts) {
        RegistrationTrackUtils.fromInstance(extensionsSource, "lookup (service loader)", exts);
        return Arrays.asList(exts);
    }

    /**
     * Register default setup objects (which must be registered last due to AOP usage).
     *
     * @param exts setup objects
     * @return setup objects instances
     */
    @SafeVarargs
    public final List<TestEnvironmentSetup> defaultExtensions(final TestEnvironmentSetup... exts) {
        RegistrationTrackUtils.fromInstance(extensionsSource, "default extension", exts);
        return Arrays.asList(exts);
    }

    /**
     * Register hook instances.
     *
     * @param exts hooks
     */
    public final void hookInstances(final GuiceyConfigurationHook... exts) {
        RegistrationTrackUtils.fromInstance(hooksSource, String.format("%s.hooks(obj)", getHookContext()), exts);
    }

    /**
     * Register hook classes.
     *
     * @param exts hooks
     */
    @SafeVarargs
    public final void hookClasses(final Class<? extends GuiceyConfigurationHook>... exts) {
        RegistrationTrackUtils.fromClass(hooksSource, String.format("%s.hooks(class)", getHookContext()), exts, false);
    }

    /**
     * Register config modifiers from annotation.
     *
     * @param ann  annotation type
     * @param exts modifiers
     */
    @SafeVarargs
    public final void configModifiersFromAnnotation(final Class<? extends Annotation> ann,
                                                    final Class<? extends ConfigModifier>... exts) {
        RegistrationTrackUtils.fromClass(configModifierSource, "@" + ann.getSimpleName() + "(configModifiers)",
                exts, true);
    }

    /**
     * Register config modifier classes.
     *
     * @param mods modifiers
     */
    @SafeVarargs
    public final void configModifierClasses(final Class<? extends ConfigModifier>... mods) {
        RegistrationTrackUtils.fromClass(configModifierSource, String.format("%s.configModifiers(class)",
                getHookContext()), mods, false);
    }

    /**
     * Register config modifier instances.
     *
     * @param exts modifiers
     */
    public final void configModifierInstances(final ConfigModifier... exts) {
        RegistrationTrackUtils.fromInstance(configModifierSource, String.format("%s.configModifiers(obj)",
                getHookContext()), exts);
    }

    /**
     * Indicate current phase.
     *
     * @param context junit context
     * @param phase   phase
     */
    public void lifecyclePhase(final ExtensionContext context, final GuiceyTestTime phase) {
        testPhase = phase;
    }

    /**
     * Record performance.
     *
     * @param name     tracker name
     * @param duration duration
     */
    public void performanceTrack(final GuiceyTestTime name, final Duration duration) {
        PerformanceTrack track = performance.stream()
                .filter(tr -> tr.phase == testPhase && tr.name == name)
                .findFirst().orElse(null);
        if (track == null) {
            track = new PerformanceTrack(name, testPhase);
            performance.add(track);
        }
        track.registerDuration(duration);
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
            System.out.println(TrackerReportBuilder.buildSetupReport(configPrefix, extensionsSource, hooksSource));
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
            System.out.println(TrackerReportBuilder.buildConfigsReport(configPrefix, configModifierSource));
        }
    }

    /**
     * Record guicey time.
     *
     * @param phase   phase
     * @param context junit context
     */
    @SuppressWarnings("PMD.SystemPrintln")
    public void logGuiceyTestTime(final GuiceyTestTime phase, final ExtensionContext context) {
        if (debug) {
            System.out.println(TrackerReportBuilder.buildPerformanceReport(performance, context, phase));
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
}
