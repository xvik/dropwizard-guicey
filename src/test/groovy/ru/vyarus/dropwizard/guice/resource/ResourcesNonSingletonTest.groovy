package ru.vyarus.dropwizard.guice.resource

import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook
import ru.vyarus.dropwizard.guice.module.installer.InstallersOptions
import ru.vyarus.dropwizard.guice.support.resource.PrototypeResource
import ru.vyarus.dropwizard.guice.support.resource.ResourceSingletonCheckApplication
import ru.vyarus.dropwizard.guice.support.resource.SingletonResource
import ru.vyarus.dropwizard.guice.test.spock.UseDropwizardApp

/**
 * @author Vyacheslav Rusakov
 * @since 03.05.2018
 */
@UseDropwizardApp(value = ResourceSingletonCheckApplication, hooks = XConf)
class ResourcesNonSingletonTest extends AbstractTest {

    void cleanupSpec() {
        PrototypeResource.reset()
        SingletonResource.reset()
    }

    def "Check resource non singleton"() {

        when: "calling prototype resource"
        new URL("http://localhost:8080/prototype/").getText()
        new URL("http://localhost:8080/prototype/").getText()
        then: "resource instantiated one time, because singletons are forced"
        PrototypeResource.callCounter == 2
        PrototypeResource.creationCounter == 2

        when: "calling singleton resource"
        new URL("http://localhost:8080/singleton/").getText()
        new URL("http://localhost:8080/singleton/").getText()
        then: "resource instantiated one time"
        SingletonResource.callCounter == 2
        SingletonResource.creationCounter == 1
    }

    static class XConf implements GuiceyConfigurationHook {
        @Override
        void configure(GuiceBundle.Builder builder) {
            builder.option(InstallersOptions.ForceSingletonForJerseyExtensions, false)
        }
    }
}