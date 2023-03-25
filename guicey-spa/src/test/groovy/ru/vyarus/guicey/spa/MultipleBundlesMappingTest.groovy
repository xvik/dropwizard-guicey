package ru.vyarus.guicey.spa

import com.google.common.net.HttpHeaders
import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp

import javax.ws.rs.core.MediaType

/**
 * @author Vyacheslav Rusakov
 * @since 05.04.2017
 */
@TestDropwizardApp(value = App, restMapping = "/rest/*")
class MultipleBundlesMappingTest extends AbstractTest {

    def "Check spa mappings"() {

        when: "first"
        String res = get("/1")
        then: "index page"
        res.contains("Sample page")

        when: "second"
        res = get("/2")
        then: "index page"
        res.contains("Sample page")

        when: "admin first"
        res = adminGet("/a1")
        then: "index page"
        res.contains("Sample page")

        when: "admin second"
        res = adminGet("/a2")
        then: "index page"
        res.contains("Sample page")


        when: "accessing not existing page"
        res = get("/2/some/")
        then: "error"
        res.contains("Sample page")

        when: "accessing not existing admin page"
        res = adminGet("/a2/some/")
        then: "error"
        res.contains("Sample page")
    }

    def "Check cache header"() {
        when: "calling index"
        def res = client.targetMain('/1').request(MediaType.TEXT_HTML).get()
        then: "cache disabled"
        res.getHeaderString(HttpHeaders.CACHE_CONTROL) == 'must-revalidate,no-cache,no-store'

        when: "calling index 1"
        res = client.targetMain('/2').request(MediaType.TEXT_HTML).get()
        then: "cache disabled"
        res.getHeaderString(HttpHeaders.CACHE_CONTROL) == 'must-revalidate,no-cache,no-store'

        when: "force redirect"
        res = client.targetMain('/1/some').request(MediaType.TEXT_HTML).get()
        then: "cache disabled"
        res.getHeaderString(HttpHeaders.CACHE_CONTROL) == 'must-revalidate,no-cache,no-store'

        when: "force redirect 2"
        res = client.targetMain('/2/some').request(MediaType.TEXT_HTML).get()
        then: "cache disabled"
        res.getHeaderString(HttpHeaders.CACHE_CONTROL) == 'must-revalidate,no-cache,no-store'

        when: "direct index page"
        res = client.targetMain('/1/index.html').request(MediaType.TEXT_HTML).get()
        then: "cache enabled"
        res.getHeaderString(HttpHeaders.CACHE_CONTROL) == null

        when: "direct index page 2"
        res = client.targetMain('/2/index.html').request(MediaType.TEXT_HTML).get()
        then: "cache enabled"
        res.getHeaderString(HttpHeaders.CACHE_CONTROL) == null
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(
                            SpaBundle.app("app", "/app", "/1").build(),
                            SpaBundle.app("app2", "/app", "/2").build(),
                            SpaBundle.adminApp("aapp1", "/app", "/a1").build(),
                            SpaBundle.adminApp("aapp2", "/app", "/a2").build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}