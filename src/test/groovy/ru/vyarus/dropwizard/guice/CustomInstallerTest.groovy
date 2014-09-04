package ru.vyarus.dropwizard.guice

import com.google.inject.Injector
import com.google.inject.Key
import io.dropwizard.setup.Bootstrap
import io.dropwizard.testing.junit.DropwizardAppRule
import org.junit.Rule
import ru.vyarus.dropwizard.guice.module.installer.feature.ResourceInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.TaskInstaller
import ru.vyarus.dropwizard.guice.module.installer.internal.FeaturesHolder
import ru.vyarus.dropwizard.guice.support.CustomInstallerApplication
import ru.vyarus.dropwizard.guice.support.DisabledFeatureApplication
import ru.vyarus.dropwizard.guice.support.TestConfiguration
import ru.vyarus.dropwizard.guice.support.feature.CustomFeature
import ru.vyarus.dropwizard.guice.support.feature.DummyResource
import ru.vyarus.dropwizard.guice.support.installer.CustomInstaller

/**
 * Custom installer found by classpath scanning and install custom feature
 * @author Vyacheslav Rusakov 
 * @since 04.09.2014
 */
class CustomInstallerTest extends AbstractTest {

    @Rule
    DropwizardAppRule<TestConfiguration> RULE =
            new DropwizardAppRule<TestConfiguration>(CustomInstallerApplication.class, 'src/test/resources/ru/vyarus/dropwizard/guice/config.yml');

    def "Check custom installer"() {

        when: "application started"
        Injector injector = GuiceBundle.getInjector()
        FeaturesHolder holder = injector.getInstance(FeaturesHolder.class);

        then: "installer found and custom feature installed"
        holder.getFeatures(CustomInstaller) == [CustomFeature]
        CustomInstaller.feature
    }
}