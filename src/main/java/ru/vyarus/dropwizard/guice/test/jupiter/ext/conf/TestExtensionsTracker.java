package ru.vyarus.dropwizard.guice.test.jupiter.ext.conf;

import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.debug.util.RenderUtils;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.test.EnableHook;
import ru.vyarus.dropwizard.guice.test.jupiter.env.EnableSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup;
import ru.vyarus.dropwizard.guice.test.util.RegistrationTrackUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Tracks registration of hooks and support objects during test initialization in order to log used
 * additions (to simplify applied objects tracking).
 *
 * @author Vyacheslav Rusakov
 * @since 27.05.2022
 */
@SuppressWarnings("checkstyle:MultipleStringLiterals")
public class TestExtensionsTracker {

    private static final ThreadLocal<Class<? extends TestEnvironmentSetup>> CONTEXT = new ThreadLocal<>();

    protected final List<String> extensionsSource = new ArrayList<>();
    protected final List<String> hooksSource = new ArrayList<>();

    private final Logger logger = LoggerFactory.getLogger(TestExtensionsTracker.class);

    public static void setContextHook(final Class<? extends TestEnvironmentSetup> hook) {
        CONTEXT.set(hook);
    }

    public final void extensionsFromFields(final List<Field> fields) {
        RegistrationTrackUtils.fromField(extensionsSource, String.format("@%s field",
                EnableSetup.class.getSimpleName()), fields);
    }

    @SafeVarargs
    public final void extensionsFromAnnotation(final Class<? extends Annotation> ann,
                                               final Class<? extends TestEnvironmentSetup>... exts) {
        // sync actual extension registration order with tracking info
        final List<String> tmp = new ArrayList<>(extensionsSource);
        extensionsSource.clear();
        RegistrationTrackUtils.fromClass(extensionsSource, String.format("@%s", ann.getSimpleName()), exts);
        extensionsSource.addAll(tmp);
    }

    public final void hooksFromFields(final List<Field> fields, final boolean baseHooks) {
        if (!fields.isEmpty()) {
            // hooks from fields in base classes activated before configured hooks
            final List<String> tmp = baseHooks ? new ArrayList<>(hooksSource) : Collections.emptyList();
            if (baseHooks) {
                hooksSource.clear();
            }
            RegistrationTrackUtils.fromField(hooksSource, String.format("@%s field",
                    EnableHook.class.getSimpleName()), fields);
            hooksSource.addAll(tmp);
        }
    }

    @SafeVarargs
    public final void hooksFromAnnotation(final Class<? extends Annotation> ann,
                                          final Class<? extends GuiceyConfigurationHook>... exts) {
        RegistrationTrackUtils.fromClass(hooksSource, String.format("@%s", ann.getSimpleName()), exts);
    }

    public final void extensionInstances(final TestEnvironmentSetup... exts) {
        RegistrationTrackUtils.fromInstance(extensionsSource, String.format("@%s instance",
                RegisterExtension.class.getSimpleName()), exts);
    }

    @SafeVarargs
    public final void extensionClasses(final Class<? extends TestEnvironmentSetup>... exts) {
        RegistrationTrackUtils.fromClass(extensionsSource, String.format("@%s class",
                RegisterExtension.class.getSimpleName()), exts);
    }

    public final void hookInstances(final GuiceyConfigurationHook... exts) {
        RegistrationTrackUtils.fromInstance(hooksSource, String.format("%s instance", getHookContext()), exts);
    }

    @SafeVarargs
    public final void hookClasses(final Class<? extends GuiceyConfigurationHook>... exts) {
        RegistrationTrackUtils.fromClass(hooksSource, String.format("%s class", getHookContext()), exts);
    }

    /**
     * Logs registered setup objects. Do nothing if no setup objects registered.
     */
    public void logExtensionRegistrations() {
        if (!extensionsSource.isEmpty() || !hooksSource.isEmpty()) {
            final StringBuilder res = new StringBuilder(500).append("Guicey test extensions:\n\n");
            if (!extensionsSource.isEmpty()) {
                res.append("\tSetup objects = \n");
                logTracks(res, extensionsSource);
            }

            if (!hooksSource.isEmpty()) {
                res.append("\tTest hooks = \n");
                logTracks(res, hooksSource);
            }

            // note: at this stage dropwizard did not apply its logger config, so message would look different
            logger.info(res.toString());
        }
    }

    private String getHookContext() {
        // hook might be registered from manual extension in filed or within setup object and in this case
        // tracking setup object class
        return CONTEXT.get() != null
                ? RenderUtils.getClassName(CONTEXT.get()) : "@" + RegisterExtension.class.getSimpleName();
    }

    private void logTracks(final StringBuilder res, final List<String> tracks) {
        for (String st : tracks) {
            res.append("\t\t").append(st).append('\n');
        }
        res.append('\n');
    }
}
