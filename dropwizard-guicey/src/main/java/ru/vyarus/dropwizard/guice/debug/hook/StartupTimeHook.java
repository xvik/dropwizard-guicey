package ru.vyarus.dropwizard.guice.debug.hook;

import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;

/**
 * Hook enables startup time logs. It is assumed to be used to enable startup time logs for compiled application
 * with the system property: {@code -Dguicey.hooks=startup-time}.
 *
 * @author Vyacheslav Rusakov
 * @since 14.03.2025
 */
public class StartupTimeHook implements GuiceyConfigurationHook {

    public static final String ALIAS = "startup-time";

    @Override
    public void configure(final GuiceBundle.Builder builder) {
        builder.printStartupTime();
    }
}
