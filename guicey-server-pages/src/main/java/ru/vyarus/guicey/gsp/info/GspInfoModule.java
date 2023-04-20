package ru.vyarus.guicey.gsp.info;

import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.module.context.SharedConfigurationState;
import ru.vyarus.dropwizard.guice.module.context.unique.item.UniqueModule;
import ru.vyarus.dropwizard.guice.module.support.EnvironmentAwareModule;
import ru.vyarus.guicey.gsp.ServerPagesBundle;
import ru.vyarus.guicey.gsp.app.GlobalConfig;

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
        final GlobalConfig config = SharedConfigurationState.lookupOrFail(environment, ServerPagesBundle.class,
                "%s bundle not registered", ServerPagesBundle.class.getSimpleName());
        bind(GspInfoService.class).toInstance(new GspInfoService(config));
    }

    @Override
    public void setEnvironment(final Environment environment) {
        this.environment = environment;
    }
}
