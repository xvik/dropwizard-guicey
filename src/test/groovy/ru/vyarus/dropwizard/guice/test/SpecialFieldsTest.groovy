package ru.vyarus.dropwizard.guice.test

import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.support.AutoScanApplication
import ru.vyarus.dropwizard.guice.support.feature.DummyResource
import ru.vyarus.dropwizard.guice.test.spock.UseDropwizardApp

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 21.05.2020
 */
@UseDropwizardApp(AutoScanApplication)
class SpecialFieldsTest extends AbstractTest {

    static GuiceyConfigurationHook hook = { builder ->
        builder.disableExtensions(DummyResource)
    } as GuiceyConfigurationHook

    static ClientSupport client

    @Inject
    GuiceyConfigurationInfo info

    def "Check static fields support"() {

        expect: "hook applied"
        info.getExtensionsDisabled().contains(DummyResource)

        and: "client injected"
        client != null
        client.getPort() == 8080
        client.getAdminPort() == 8081
    }
}
