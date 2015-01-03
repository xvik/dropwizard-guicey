package ru.vyarus.dropwizard.guice

import com.google.inject.Inject
import com.google.inject.Injector
import com.google.inject.Key
import io.dropwizard.setup.Bootstrap
import ru.vyarus.dropwizard.guice.module.installer.feature.TaskInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.ResourceInstaller
import ru.vyarus.dropwizard.guice.module.installer.internal.FeaturesHolder
import ru.vyarus.dropwizard.guice.support.DisabledFeatureApplication
import ru.vyarus.dropwizard.guice.support.feature.DummyResource
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp

/**
 * @author Vyacheslav Rusakov 
 * @since 04.09.2014
 */
@UseGuiceyApp(DisabledFeatureApplication)
class DisabledFeatureTest extends AbstractTest {

    @Inject
    FeaturesHolder holder
    @Inject
    Bootstrap bootstrap
    @Inject
    Injector injector

    def "Check disabled feature and no command search"() {

        when: "application started"

        then: "command search not enabled"
        bootstrap.getCommands() == []

        then: "task not found"
        holder.getFeatures(TaskInstaller) == null

        then: "resource found"
        holder.getFeatures(ResourceInstaller) == [DummyResource]
        injector.getExistingBinding(Key.get(DummyResource))
    }
}