package ru.vyarus.dropwizard.guice.bundles.manual

import io.dropwizard.Application
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.support.TestConfiguration
import ru.vyarus.dropwizard.guice.support.feature.DummyManaged
import ru.vyarus.dropwizard.guice.support.feature.DummyResource
import ru.vyarus.dropwizard.guice.support.feature.DummyTask
import ru.vyarus.dropwizard.guice.support.util.GuiceRestrictedConfigBundle

/**
 * @author Vyacheslav Rusakov 
 * @since 02.08.2015
 */
class ManualBundlesApplication extends Application<TestConfiguration> {

    @Override
    void initialize(Bootstrap<TestConfiguration> bootstrap) {
        bootstrap.addBundle(GuiceBundle.builder()
                .disableBundles(GuiceRestrictedConfigBundle)
                .extensions(DummyTask, DummyResource, DummyManaged)
                .build()
        );
    }

    @Override
    void run(TestConfiguration configuration, Environment environment) throws Exception {

    }
}
