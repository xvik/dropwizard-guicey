package ru.vyarus.dropwizard.guice.web

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.support.web.servletclash.Servlet1
import ru.vyarus.dropwizard.guice.support.web.servletclash.Servlet2
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp

/**
 * @author Vyacheslav Rusakov
 * @since 08.08.2016
 */
@TestDropwizardApp(ClashApp)
class ServletMappingClashTest extends AbstractTest {

    def "Check servlets mapping clash"() {

        expect: "it's only a warning"
        true
    }

    static class ClashApp extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .extensions(Servlet1, Servlet2)
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {

        }
    }
}
