package ru.vyarus.dropwizard.guice.test

import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.support.AutoScanApplication
import ru.vyarus.dropwizard.guice.test.spock.UseDropwizardApp

import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.core.Response

/**
 * @author Vyacheslav Rusakov
 * @since 20.05.2020
 */
@UseDropwizardApp(value = AutoScanApplication.class, restMapping = "api")
class CustomRestMappingTest extends AbstractTest {

    def "Check custom rest prefix"() {
        Response response = ClientBuilder.newClient()
                .target("http://localhost:8080/api/dummy/")
                .request()
                .buildGet()
                .invoke();

        expect: "prefix applied"
        response.status == 200
    }
}
