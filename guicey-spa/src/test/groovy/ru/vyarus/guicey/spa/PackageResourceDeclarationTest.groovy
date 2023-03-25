package ru.vyarus.guicey.spa

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp

/**
 * @author Vyacheslav Rusakov
 * @since 05.12.2019
 */
@TestDropwizardApp(value = App, restMapping = "/rest/*")
class PackageResourceDeclarationTest extends AbstractTest {

    def "Check resources mapped"() {

        when: "accessing resource"
        String res = get("/some.css")
        then: "style"
        res == "/* styles */"

    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(SpaBundle.app("app", "app.css", "/").build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
