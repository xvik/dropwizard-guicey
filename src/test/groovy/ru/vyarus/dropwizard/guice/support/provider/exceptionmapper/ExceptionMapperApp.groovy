package ru.vyarus.dropwizard.guice.support.provider.exceptionmapper

import io.dropwizard.core.Application
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.ResourceInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.provider.JerseyProviderInstaller
import ru.vyarus.dropwizard.guice.support.TestConfiguration

/**
 * @author Vyacheslav Rusakov 
 * @since 17.04.2015
 */
class ExceptionMapperApp extends Application<TestConfiguration> {

    @Override
    void initialize(Bootstrap<TestConfiguration> bootstrap) {
        bootstrap.addBundle(GuiceBundle.builder()
                .noDefaultInstallers()
                .installers(JerseyProviderInstaller, ResourceInstaller)
                .extensions(IOExceptionMapper, ExceptionResource)
                .build());
    }

    @Override
    void run(TestConfiguration configuration, Environment environment) throws Exception {
    }
}
