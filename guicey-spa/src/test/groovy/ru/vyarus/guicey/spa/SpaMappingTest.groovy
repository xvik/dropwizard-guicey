package ru.vyarus.guicey.spa

import com.google.common.net.HttpHeaders
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.ClientSupport
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp

import javax.ws.rs.core.MediaType

/**
 * @author Vyacheslav Rusakov
 * @since 02.04.2017
 */
@TestDropwizardApp(value = App, restMapping = "/rest/*")
class SpaMappingTest extends AbstractTest {

    def "Check spa mapped"() {

        when: "accessing app"
        String res = get("/")
        then: "index page"
        res.contains("Sample page")

        when: "accessing not existing page"
        res = get("/some/")
        then: "ok"
        res.contains("Sample page")

        when: "accessing not existing resource"
        get("/some.html")
        then: "error"
        thrown(FileNotFoundException)
    }

    def "Check no cache header"(ClientSupport client) {

        when: "calling index"
        def res = client.targetMain('/').request(MediaType.TEXT_HTML).get()
        then: "cache disabled"
        res.getHeaderString(HttpHeaders.CACHE_CONTROL) == 'must-revalidate,no-cache,no-store'

        when: "force redirect"
        res = client.targetMain('/some').request(MediaType.TEXT_HTML).get()
        then: "cache disabled"
        res.getHeaderString(HttpHeaders.CACHE_CONTROL) == 'must-revalidate,no-cache,no-store'

        when: "direct index page"
        res = client.targetMain('/index.html').request(MediaType.TEXT_HTML).get()
        then: "cache enabled"
        res.getHeaderString(HttpHeaders.CACHE_CONTROL) == null

        when: "resource"
        res = client.targetMain('/css/some.css').request().get()
        then: "cache enabled"
        res.getHeaderString(HttpHeaders.CACHE_CONTROL) == null
    }

    def "Chck different mime type"() {

        when: "calling with html type"
        def res = client.targetMain('/some').request(MediaType.TEXT_HTML).get()
        then: "redirect"
        res.status == 200

        when: "calling with text type"
        res = client.targetMain('/some').request(MediaType.TEXT_PLAIN).get()
        then: "no redirect"
        res.status == 404

        when: "calling with unknown content type"
        res = client.targetMain('/some').request("abrakadabra").get()
        then: "no redirect"
        res.status == 404

        when: "calling with empty type"
        res = client.targetMain('/some').request(" ").get()
        then: "no redirect"
        res.status == 404

    }

    def "Check non 404 error"() {

        when: "calling for cached content"
        def res = client.targetMain('/index.html').request(MediaType.TEXT_HTML)
                .header('If-Modified-Since', 'Wed, 21 Oct 2215 07:28:00 GMT').get()
        then: "cache disabled"
        res.status == 304

    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(SpaBundle.app("app", "app", "/").build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}