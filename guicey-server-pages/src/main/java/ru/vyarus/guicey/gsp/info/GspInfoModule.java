package ru.vyarus.guicey.gsp.info;

import io.dropwizard.core.setup.Environment;
import ru.vyarus.dropwizard.guice.module.context.SharedConfigurationState;
import ru.vyarus.dropwizard.guice.module.context.unique.item.UniqueModule;
import ru.vyarus.dropwizard.guice.module.support.EnvironmentAwareModule;
import ru.vyarus.guicey.gsp.app.ServerPagesGlobalState;

/**
 * GSP registrations Information module.
 *
 * @author Vyacheslav Rusakov
 * @since 03.12.2019
 */
public class GspInfoModule extends UniqueModule implements EnvironmentAwareModule {

    private Environment environment;

    @Override
    protected void configure() {
        final ServerPagesGlobalState config = SharedConfigurationState
                .lookupOrFail(environment, ServerPagesGlobalState.class,
                "%s bundle not registered", ServerPagesGlobalState.class.getSimpleName());
        bind(GspInfoService.class).toInstance(new GspInfoService(config));
    }

    @Override
    public void setEnvironment(final Environment environment) {
        this.environment = environment;
    }
}
