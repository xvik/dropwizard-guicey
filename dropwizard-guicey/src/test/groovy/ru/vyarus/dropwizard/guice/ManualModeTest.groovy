package ru.vyarus.dropwizard.guice

import com.google.inject.Inject
import com.google.inject.Injector
import com.google.inject.Key
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.installer.feature.ManagedInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.TaskInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.ResourceInstaller
import ru.vyarus.dropwizard.guice.support.ManualApplication
import ru.vyarus.dropwizard.guice.support.TestConfiguration
import ru.vyarus.dropwizard.guice.support.feature.DummyManaged
import ru.vyarus.dropwizard.guice.support.feature.DummyResource
import ru.vyarus.dropwizard.guice.support.feature.DummyTask
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov 
 * @since 04.09.2014
 */
@TestGuiceyApp(ManualApplication)
class ManualModeTest extends Specification {

    @Inject
    GuiceyConfigurationInfo info
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
        info.installers.size() == 3

        then: "command injection done"
        bootstrap.getCommands()[0].service

        then: "task found"
        info.getExtensions(TaskInstaller) == [DummyTask]
        injector.getExistingBinding(Key.get(DummyTask))

        then: "resource found"
        info.getExtensions(ResourceInstaller) == [DummyResource]
        injector.getExistingBinding(Key.get(DummyResource))

        then: "managed found"
        info.getExtensions(ManagedInstaller) == [DummyManaged]
        injector.getExistingBinding(Key.get(DummyManaged))
    }
}