package ru.vyarus.dropwizard.guice

import com.google.inject.Injector
import com.google.inject.Key
import io.dropwizard.setup.Bootstrap
import io.dropwizard.testing.junit.DropwizardAppRule
import org.junit.Rule
import ru.vyarus.dropwizard.guice.module.installer.feature.ResourceInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.TaskInstaller
import ru.vyarus.dropwizard.guice.module.installer.internal.FeaturesHolder
import ru.vyarus.dropwizard.guice.support.DisabledFeatureApplication
import ru.vyarus.dropwizard.guice.support.TestConfiguration
import ru.vyarus.dropwizard.guice.support.feature.DummyResource

/**
 * @author Vyacheslav Rusakov 
 * @since 04.09.2014
 */
class DisabledFeatureTest extends AbstractTest {

    @Rule
    DropwizardAppRule<TestConfiguration> RULE =
            new DropwizardAppRule<TestConfiguration>(DisabledFeatureApplication.class, 'src/test/resources/ru/vyarus/dropwizard/guice/config.yml');

    def "Check disabled feature and no command search"() {

        when: "application started"
        Injector injector = GuiceBundle.getInjector()
        FeaturesHolder holder = injector.getInstance(FeaturesHolder.class);
        Bootstrap bootstrap = injector.getInstance(Bootstrap.class);

        then: "command search not enabled"
        bootstrap.getCommands() == []

        then: "task not found"
        holder.getFeatures(TaskInstaller) == null

        then: "resource found"
        holder.getFeatures(ResourceInstaller) == [DummyResource]
        injector.getExistingBinding(Key.get(DummyResource))
    }
}