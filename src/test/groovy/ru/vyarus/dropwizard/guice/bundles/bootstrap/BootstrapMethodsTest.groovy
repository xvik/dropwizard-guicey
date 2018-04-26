package ru.vyarus.dropwizard.guice.bundles.bootstrap

import com.google.inject.Inject
import com.google.inject.Injector
import com.google.inject.Key
import com.google.inject.name.Names
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.installer.feature.ManagedInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.TaskInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.ResourceInstaller
import ru.vyarus.dropwizard.guice.support.feature.DummyManaged
import ru.vyarus.dropwizard.guice.support.feature.DummyResource
import ru.vyarus.dropwizard.guice.support.feature.DummyTask
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp

/**
 * @author Vyacheslav Rusakov 
 * @since 03.08.2015
 */
@UseGuiceyApp(GBootstrapApplication)
class BootstrapMethodsTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info
    @Inject
    Injector injector

    def "Check bootstrap config"() {

        when: "application started"

        then: "all registered installers found"
        info.installers.size() == 7

        then: "task found"
        info.getExtensions(TaskInstaller) == [DummyTask]
        injector.getExistingBinding(Key.get(DummyTask))

        then: "resource found"
        info.getExtensions(ResourceInstaller) == [DummyResource]
        injector.getExistingBinding(Key.get(DummyResource))

        then: "managed found"
        info.getExtensions(ManagedInstaller) == [DummyManaged]
        injector.getExistingBinding(Key.get(DummyManaged))

        then: "modules bound"
        injector.getInstance(Key.get(String, Names.named("sample"))) == 'test str'
        injector.getInstance(Key.get(String, Names.named("sample2"))) == 'test str override'

    }
}