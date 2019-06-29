package ru.vyarus.dropwizard.guice.web

import com.google.inject.CreationException
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import io.dropwizard.testing.junit.DropwizardAppRule
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.support.web.servletclash.Servlet1
import ru.vyarus.dropwizard.guice.support.web.servletclash.Servlet2

import static ru.vyarus.dropwizard.guice.module.installer.InstallersOptions.DenyServletRegistrationWithClash

/**
 * @author Vyacheslav Rusakov
 * @since 20.08.2016
 */
class ServletMappingClashFailTest extends AbstractTest {

    def "Check servlets mapping clash"() {

        when: "starting app with servlets clash"
        new DropwizardAppRule(ClashApp).before()
        then: "exception thrown"
        def ex = thrown(CreationException)
        ex.cause.message == "Servlet registration Servlet2 clash with already installed servlets on paths: /sam"
    }

    static class ClashApp extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .extensions(Servlet1, Servlet2)
                    .option(DenyServletRegistrationWithClash, true)
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {

        }
    }

}