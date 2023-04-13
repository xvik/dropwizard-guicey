package ru.vyarus.dropwizard.guice.test

import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.support.AutoScanApplication
import ru.vyarus.dropwizard.guice.support.feature.DummyResource
import ru.vyarus.dropwizard.guice.test.spock.InjectClient
import ru.vyarus.dropwizard.guice.test.spock.UseDropwizardApp
import spock.lang.Shared
import spock.lang.Specification

import jakarta.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 21.05.2020
 */
@UseDropwizardApp(AutoScanApplication)
class SpecialFieldsTest extends Specification {

    @EnableHook
    static GuiceyConfigurationHook hook = { builder ->
        builder.disableExtensions(DummyResource)
    } as GuiceyConfigurationHook

    @InjectClient
    static ClientSupport client

    @InjectClient
    @Shared
    ClientSupport clientShared

    @InjectClient
    ClientSupport clientInstance

    @Inject
    GuiceyConfigurationInfo info

    def "Check static fields support"() {

        expect: "hook applied"
        info.getExtensionsDisabled().contains(DummyResource)

        and: "static client injected"
        client != null
        client.getPort() == 8080
        client.getAdminPort() == 8081

        and: "non static injections"
        clientShared == client
        clientInstance == client
    }
}
