package ru.vyarus.dropwizard.guice.module.installer.util;

import com.google.common.collect.ImmutableList;
import ru.vyarus.dropwizard.guice.debug.util.RenderUtils;
import ru.vyarus.dropwizard.guice.module.context.SharedConfigurationState;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyEnvironment;
import ru.vyarus.dropwizard.guice.module.support.DropwizardAwareModule;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.TestDropwizardAppExtension;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.TestGuiceyAppExtension;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.conf.ExtensionBuilder;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.conf.ExtensionConfig;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.conf.track.RegistrationTrackUtils;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.conf.track.TestExtensionsTracker;

import java.util.List;
import java.util.Optional;

/**
 * Utility for obtaining source file reference on method call. Used internally to track test extensions registration
 * (to show navigation links in the debug report).
 *
 * @author Vyacheslav Rusakov
 * @since 06.03.2025
 */
public final class StackUtils {

    private static final StackWalker WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
    private static final List<Class<?>> SHARED_STATE_INFRA = ImmutableList.of(
            SharedConfigurationState.class,
            GuiceyBootstrap.class,
            GuiceyEnvironment.class,
            DropwizardAwareModule.class,
            Optional.class
    );
    private static final List<Class<?>> EXT_INFRA = ImmutableList.of(
            ExtensionBuilder.class,
            TestExtensionsTracker.class,
            ExtensionConfig.class,
            RegistrationTrackUtils.class,
            TestGuiceyAppExtension.class,
            TestDropwizardAppExtension.class);

    private StackUtils() {
    }

    /**
     * @param skip classes to skip in stack
     * @return caller stack frame
     */
    public static Optional<StackWalker.StackFrame> getCaller(final List<Class<?>> skip) {
        return WALKER.walk(stream ->
                stream.dropWhile(frame -> {
                    final Class<?> type = frame.getDeclaringClass();
                    return type.equals(StackUtils.class)
                            || type.getPackage().getName().startsWith("org.codehaus.groovy.vmplugin")
                            || skip.contains(type)
                            || skip.contains(type.getEnclosingClass());
                }).findFirst()
        );
    }

    /**
     * @param skip classes to skip in stack
     * @return formatted caller source
     */
    public static String getCallerSource(final List<Class<?>> skip) {
        final StackWalker.StackFrame frame = getCaller(skip).orElse(null);
        return "at " + (frame != null ? RenderUtils.renderPackage(frame.getDeclaringClass())
                + ".(" + frame.getFileName() + ":" + frame.getLineNumber() + ")" : "unknown source");
    }

    /**
     * Pre-configured to skip guicey test extension classes.
     *
     * @return calling source
     */
    public static String getTestExtensionSource() {
        return getCallerSource(EXT_INFRA);
    }

    /**
     * @return shared state calling source
     */
    public static String getSharedStateSource() {
        return getCallerSource(SHARED_STATE_INFRA);
    }
}
