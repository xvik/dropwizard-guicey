package ru.vyarus.dropwizard.guice.support.provider.exceptionmapper

import io.dropwizard.Application
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.ResourceInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.provider.JerseyProviderInstaller
import ru.vyarus.dropwizard.guice.support.TestConfiguration

/**
 * @author Vyacheslav Rusakov 
 * @since 17.04.2015
 */
class ExceptionMapperApp2 extends Application<TestConfiguration> {

    @Override
    void initialize(Bootstrap<TestConfiguration> bootstrap) {
        bootstrap.addBundle(GuiceBundle.<TestConfiguration> builder()
                .noDefaultInstallers()
                .installers(JerseyProviderInstaller, ResourceInstaller)
                .extensions(HkManagedExceptionMapper, ExceptionResource)
                .build());
    }

    @Override
    void run(TestConfiguration configuration, Environment environment) throws Exception {
    }
}
