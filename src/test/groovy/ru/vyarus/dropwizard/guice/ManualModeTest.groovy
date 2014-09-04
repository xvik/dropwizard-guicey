package ru.vyarus.dropwizard.guice

import com.google.inject.Injector
import com.google.inject.Key
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import io.dropwizard.testing.junit.DropwizardAppRule
import org.junit.Rule
import ru.vyarus.dropwizard.guice.module.installer.feature.*
import ru.vyarus.dropwizard.guice.module.installer.feature.eager.EagerInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.health.HealthCheckInstaller
import ru.vyarus.dropwizard.guice.module.installer.internal.FeaturesHolder
import ru.vyarus.dropwizard.guice.support.ManualApplication
import ru.vyarus.dropwizard.guice.support.TestConfiguration
import ru.vyarus.dropwizard.guice.support.feature.*

/**
 * @author Vyacheslav Rusakov 
 * @since 04.09.2014
 */
class ManualModeTest extends AbstractTest {

    @Rule
    DropwizardAppRule<TestConfiguration> RULE =
            new DropwizardAppRule<TestConfiguration>(ManualApplication.class, 'src/test/resources/ru/vyarus/dropwizard/guice/config.yml');

    def "Check manual configuration"() {

        when: "application started"
        Injector injector = GuiceBundle.getInjector()
        FeaturesHolder holder = injector.getInstance(FeaturesHolder.class);
        Bootstrap bootstrap = injector.getInstance(Bootstrap.class);

        then: "environment binding done"
        bootstrap
        injector.getExistingBinding(Key.get(Environment))
        injector.getExistingBinding(Key.get(Configuration))
        injector.getExistingBinding(Key.get(TestConfiguration))

        then: "all registered installers found"
        holder.installers.size() == 3

        then: "command injection done"
        bootstrap.getCommands()[0].service

        then: "task found"
        holder.getFeatures(TaskInstaller) == [DummyTask]
        injector.getExistingBinding(Key.get(DummyTask))

        then: "resource found"
        holder.getFeatures(ResourceInstaller) == [DummyResource]
        injector.getExistingBinding(Key.get(DummyResource))

        then: "managed found"
        holder.getFeatures(ManagedInstaller) == [DummyManaged]
        injector.getExistingBinding(Key.get(DummyManaged))
    }
}