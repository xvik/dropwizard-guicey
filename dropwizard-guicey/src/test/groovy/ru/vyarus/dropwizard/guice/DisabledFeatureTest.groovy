package ru.vyarus.dropwizard.guice

import com.google.inject.Inject
import com.google.inject.Injector
import com.google.inject.Key
import io.dropwizard.core.setup.Bootstrap
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.installer.feature.TaskInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.ResourceInstaller
import ru.vyarus.dropwizard.guice.support.DisabledFeatureApplication
import ru.vyarus.dropwizard.guice.support.feature.DummyResource
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov 
 * @since 04.09.2014
 */
@TestGuiceyApp(DisabledFeatureApplication)
class DisabledFeatureTest extends Specification {

    @Inject
    GuiceyConfigurationInfo info
    @Inject
    Bootstrap bootstrap
    @Inject
    Injector injector

    def "Check disabled feature and no command search"() {

        when: "application started"

        then: "command search not enabled"
        bootstrap.getCommands() == []

        then: "task not found"
        info.getExtensions(TaskInstaller).isEmpty()

        then: "resource found"
        info.getExtensions(ResourceInstaller) == [DummyResource]
        injector.getExistingBinding(Key.get(DummyResource))
    }
}