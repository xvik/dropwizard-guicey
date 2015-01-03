package ru.vyarus.dropwizard.guice

import com.google.inject.Inject
import com.google.inject.Injector
import com.google.inject.Key
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.module.installer.feature.ManagedInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.TaskInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.ResourceInstaller
import ru.vyarus.dropwizard.guice.module.installer.internal.FeaturesHolder
import ru.vyarus.dropwizard.guice.support.ManualApplication
import ru.vyarus.dropwizard.guice.support.TestConfiguration
import ru.vyarus.dropwizard.guice.support.feature.DummyManaged
import ru.vyarus.dropwizard.guice.support.feature.DummyResource
import ru.vyarus.dropwizard.guice.support.feature.DummyTask
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp

/**
 * @author Vyacheslav Rusakov 
 * @since 04.09.2014
 */
@UseGuiceyApp(ManualApplication)
class ManualModeTest extends AbstractTest {

    @Inject
    FeaturesHolder holder
    @Inject
    Bootstrap bootstrap
    @Inject
    Injector injector

    def "Check manual configuration"() {

        when: "application started"

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