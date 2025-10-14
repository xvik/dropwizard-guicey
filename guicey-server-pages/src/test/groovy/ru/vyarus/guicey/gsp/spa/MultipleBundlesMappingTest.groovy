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

import javax.ws.rs.core.MediaType

/**
 * @author Vyacheslav Rusakov
 * @since 18.01.2019
 */
@TestDropwizardApp(value = App, restMapping = "/rest/*")
class MultipleBundlesMappingTest extends AbstractTest {

    def "Check spa mappings"() {

        when: "first"
        String res = getHtml("/1")
        then: "index page"
        res.contains("Sample page")

        when: "second"
        res = getHtml("/2")
        then: "index page"
        res.contains("Sample page")

        when: "admin first"
        res = adminGetHtml("/a1")
        then: "index page"
        res.contains("Sample page")

        when: "admin second"
        res = adminGetHtml("/a2")
        then: "index page"
        res.contains("Sample page")


        when: "accessing not existing page"
        res = getHtml("/2/some/")
        then: "error"
        res.contains("Sample page")

        when: "accessing not existing admin page"
        res = adminGetHtml("/a2/some/")
        then: "error"
        res.contains("Sample page")
    }

    def "Check cache header"(ClientSupport client) {

        when: "calling first path"
        def res = client.targetApp('/1').request(MediaType.TEXT_HTML).get()
        then: "cache disabled"
        res.getHeaderString(HttpHeaders.CACHE_CONTROL) == 'must-revalidate,no-cache,no-store'

        when: "calling second path"
        res = client.targetApp('/2').request(MediaType.TEXT_HTML).get()
        then: "cache disabled"
        res.getHeaderString(HttpHeaders.CACHE_CONTROL) == 'must-revalidate,no-cache,no-store'

        when: "force redirect"
        res = client.targetApp('/1/some').request(MediaType.TEXT_HTML).get()
        then: "cache disabled"
        res.getHeaderString(HttpHeaders.CACHE_CONTROL) == 'must-revalidate,no-cache,no-store'

        when: "force redirect 2"
        res = client.targetApp('/2/some').request(MediaType.TEXT_HTML).get()
        then: "cache disabled"
        res.getHeaderString(HttpHeaders.CACHE_CONTROL) == 'must-revalidate,no-cache,no-store'

        when: "direct index page"
        res = client.targetApp('/1/index.html').request(MediaType.TEXT_HTML).get()
        then: "cache enabled"
        res.getHeaderString(HttpHeaders.CACHE_CONTROL) == null

        when: "direct index page 2"
        res = client.targetApp('/2/index.html').request(MediaType.TEXT_HTML).get()
        then: "cache enabled"
        res.getHeaderString(HttpHeaders.CACHE_CONTROL) == null
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(
                            ServerPagesBundle.builder().build(),
                            ServerPagesBundle.app("app1", "/app", "/1")
                                    .indexPage('index.html')
                                    .spaRouting()
                                    .build(),
                            ServerPagesBundle.app("app2", "/app", "/2")
                                    .indexPage('index.html')
                                    .spaRouting()
                                    .build(),
                            ServerPagesBundle.adminApp("aapp1", "/app", "/a1")
                                    .indexPage('index.html')
                                    .spaRouting()
                                    .build(),
                            ServerPagesBundle.adminApp("aapp2", "/app", "/a2")
                                    .indexPage('index.html')
                                    .spaRouting()
                                    .build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}