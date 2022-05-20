package ru.vyarus.dropwizard.guice.test.jupiter.ext.conf;

import io.dropwizard.testing.ConfigOverride;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.test.jupiter.env.EnableSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup;
import ru.vyarus.dropwizard.guice.test.util.RegistrationTrackUtils;
import ru.vyarus.dropwizard.guice.test.util.TestSetupUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Base configuration for junit 5 extensions (contains common configurations). Required to unify common configuration
 * methods in {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.conf.ExtensionBuilder}.
 *
 * @author Vyacheslav Rusakov
 * @since 12.05.2022
 */
@SuppressWarnings({"checkstyle:VisibilityModifier", "PMD.DefaultPackage"})
public abstract class ExtensionConfig {

    public String[] configOverrides = new String[0];
    // required for lazy evaluation values
    public List<ConfigOverride> configOverrideObjects = new ArrayList<>();
    public List<GuiceyConfigurationHook> hooks;

    public List<TestEnvironmentSetup> extensions = new ArrayList<>();
    // tracks source of registered setup objects
    protected List<String> extensionsSource = new ArrayList<>();

    private final Logger logger = LoggerFactory.getLogger(ExtensionConfig.class);

    public final void extensionsFromFields(final List<TestEnvironmentSetup> exts, final List<Field> fields) {
        extensions.addAll(exts);
        // track
        RegistrationTrackUtils.fromField(extensionsSource, String.format("@%s field",
                EnableSetup.class.getSimpleName()), fields);
    }

    @SafeVarargs
    public final void extensionsFromAnnotation(final Class<? extends Annotation> ann,
                                               final Class<? extends TestEnvironmentSetup>... exts) {
        extensions.addAll(TestSetupUtils.create(exts));
        //track
        RegistrationTrackUtils.fromClass(extensionsSource, String.format("@%s", ann.getSimpleName()), exts);
    }

    /**
     * Logs registered setup objects. Do nothing if no setup objects registered.
     */
    public void logExtensionRegistrations() {
        if (!extensionsSource.isEmpty()) {
            // logger not used because it is not yet configured (message will be visible but with different pattern)
            final StringBuilder res = new StringBuilder("Guicey test setup objects = \n\n");
            for (String st : extensionsSource) {
                res.append('\t').append(st).append('\n');
            }
            // note: at this stage dropwizard did not apply its logger config, so message would look different
            logger.info(res.toString());
        }
    }
}
