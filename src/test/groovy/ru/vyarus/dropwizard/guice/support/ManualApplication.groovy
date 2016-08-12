package ru.vyarus.dropwizard.guice.support

import io.dropwizard.Application
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.installer.feature.ManagedInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.ResourceInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.TaskInstaller
import ru.vyarus.dropwizard.guice.support.feature.DummyCommand
import ru.vyarus.dropwizard.guice.support.feature.DummyManaged
import ru.vyarus.dropwizard.guice.support.feature.DummyResource
import ru.vyarus.dropwizard.guice.support.feature.DummyTask

/**
 * Example of manually configured application (no classpath scan)
 * @author Vyacheslav Rusakov 
 * @since 04.09.2014
 */
class ManualApplication extends Application<TestConfiguration> {

    public static void main(String[] args) {
        new ManualApplication().run(args)
    }

    @Override
    void initialize(Bootstrap<TestConfiguration> bootstrap) {
        bootstrap.addBundle(GuiceBundle.<TestConfiguration> builder()
                .disableBundleLookup()
                .noDefaultInstallers()
                .installers(ResourceInstaller, TaskInstaller, ManagedInstaller)
                .extensions(DummyTask, DummyResource, DummyManaged)
                .build()
        );
        bootstrap.addCommand(new DummyCommand(bootstrap.getApplication()))
    }

    @Override
    void run(TestConfiguration configuration, Environment environment) throws Exception {
    }
}
