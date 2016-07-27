package ru.vyarus.dropwizard.guice.bundles.dwbundle

import io.dropwizard.Application
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.bundle.lookup.VoidBundleLookup
import ru.vyarus.dropwizard.guice.module.installer.CoreInstallersBundle
import ru.vyarus.dropwizard.guice.support.TestConfiguration
import ru.vyarus.dropwizard.guice.support.feature.DummyManaged
import ru.vyarus.dropwizard.guice.support.feature.DummyResource
import ru.vyarus.dropwizard.guice.support.feature.DummyTask

/**
 * @author Vyacheslav Rusakov 
 * @since 02.08.2015
 */
class DwBundleApplication extends Application<TestConfiguration>{

    @Override
    void initialize(Bootstrap<TestConfiguration> bootstrap) {
        bootstrap.addBundle(GuiceBundle.<TestConfiguration> builder()
                .disableBundleLookup()
                .configureFromDropwizardBundles()
                .build()
        );
        bootstrap.addBundle(new Bundle1())
        bootstrap.addBundle(new Bundle2())
    }

    @Override
    void run(TestConfiguration configuration, Environment environment) throws Exception {
    }
}
