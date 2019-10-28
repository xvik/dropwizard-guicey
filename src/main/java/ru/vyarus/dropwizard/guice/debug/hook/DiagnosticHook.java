package ru.vyarus.dropwizard.guice.debug.hook;

import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;

/**
 * Hook enables diagnostic logs. It is assumed to be used to enable diagnostic logs for compiled application
 * with the system property: {@code -Dguicey.hooks=diagnostic}.
 *
 * @author Vyacheslav Rusakov
 * @since 16.08.2019
 */
public class DiagnosticHook implements GuiceyConfigurationHook {

    @Override
    public void configure(final GuiceBundle.Builder builder) {
        builder.printDiagnosticInfo()
                .printLifecyclePhasesDetailed()
                .printCustomConfigurationBindings()
                .printGuiceBindings()
                .printWebMappings()
                .printJerseyConfig();
    }
}
