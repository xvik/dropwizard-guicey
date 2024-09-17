package ru.vyarus.guicey.gsp.spa

import com.google.common.net.HttpHeaders
import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.ClientSupport
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import ru.vyarus.guicey.gsp.AbstractTest
import ru.vyarus.guicey.gsp.ServerPagesBundle

import jakarta.ws.rs.core.MediaType

/**
 * @author Vyacheslav Rusakov
 * @since 14.01.2019
 */
@TestDropwizardApp(value = App, restMapping = "/rest/*")
class SpaRoutingTest extends AbstractTest {

    def "Check spa mapped"() {

        when: "accessing app"
        String res = getHtml("/")
        then: "index page"
        res.contains("Sample page")

        when: "accessing not existing page"
        res = getHtml("/some/")
        then: "ok"
        res.contains("Sample page")

        when: "accessing not existing resource"
        getHtml("/some.html")
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
        res = client.targetMain('/css/style.css').request().get()
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

        when: "calling with empty type not allowed"
        res = client.targetMain('/some').request(" ").get()
        then: "empty response type "
        res.status == 404

    }

    def "Check non 404 error"() {

        when: "calling for cached content"
        def res = client.targetMain('/index.html').request(MediaType.TEXT_HTML)
                .header('If-Modified-Since', 'Wed, 21 Oct 2215 07:28:00 GMT').get()
        then: "cached"
        res.status == 304

    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(
                            ServerPagesBundle.builder().build(),
                            ServerPagesBundle.app("app", "/app", "/")
                                    .indexPage("index.html")
                                    .spaRouting()
                                    .build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

}
