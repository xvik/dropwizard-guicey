package ru.vyarus.dropwizard.guice.cases.ifaceresource

import org.glassfish.jersey.client.proxy.WebResourceFactory
import ru.vyarus.dropwizard.guice.cases.ifaceresource.support.InterfaceResourceApp
import ru.vyarus.dropwizard.guice.cases.ifaceresource.support.ResourceContract
import ru.vyarus.dropwizard.guice.cases.ifaceresource.support.ResourceImpl
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.ResourceInstaller
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import spock.lang.Specification

import jakarta.inject.Inject
import jakarta.ws.rs.client.Client
import jakarta.ws.rs.client.ClientBuilder

/**
 * @author Vyacheslav Rusakov
 * @since 18.06.2016
 */
@TestDropwizardApp(InterfaceResourceApp)
class InterfaceResourceDefinitionTest extends Specification {

    @Inject
    GuiceyConfigurationInfo info

    def "Check resources recognition"() {

        expect: "only one resource recognized"
        info.getExtensions(ResourceInstaller) == [ResourceImpl]
    }

    def "Check resource registered"() {

        expect: 'resource called'
        new URL("http://localhost:8080/res").getText() == 'called!'
    }

    def "Check resource proxy usage"() {

        // https://jersey.java.net/apidocs/2.22.1/jersey/org/glassfish/jersey/client/proxy/package-summary.html
        Client c = ClientBuilder.newClient();
        ResourceContract resource = WebResourceFactory.newResource(ResourceContract.class, c.target("http://localhost:8080/"));

        expect:

        resource.latest() == 'called!'
    }
}