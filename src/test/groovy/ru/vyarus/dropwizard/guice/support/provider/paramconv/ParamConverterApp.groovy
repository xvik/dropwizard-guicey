package ru.vyarus.dropwizard.guice.support.provider.paramconv

import io.dropwizard.Application
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.ResourceInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.provider.JerseyProviderInstaller
import ru.vyarus.dropwizard.guice.support.TestConfiguration

/**
 * @author Vyacheslav Rusakov 
 * @since 03.09.2015
 */
class ParamConverterApp extends Application<TestConfiguration> {

    @Override
    void initialize(Bootstrap<TestConfiguration> bootstrap) {
        bootstrap.addBundle(GuiceBundle.<TestConfiguration> builder()
                .installers(JerseyProviderInstaller, ResourceInstaller)
                .extensions(FooParamConverter, ParamResource)
                .build());
    }

    @Override
    void run(TestConfiguration configuration, Environment environment) throws Exception {
    }
}
