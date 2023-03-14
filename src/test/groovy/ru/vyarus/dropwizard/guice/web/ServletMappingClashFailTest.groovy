package ru.vyarus.dropwizard.guice.web

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.support.web.servletclash.Servlet1
import ru.vyarus.dropwizard.guice.support.web.servletclash.Servlet2
import ru.vyarus.dropwizard.guice.test.TestSupport

import static ru.vyarus.dropwizard.guice.module.installer.InstallersOptions.DenyServletRegistrationWithClash

/**
 * @author Vyacheslav Rusakov
 * @since 20.08.2016
 */
class ServletMappingClashFailTest extends AbstractTest {

    def "Check servlets mapping clash"() {

        when: "starting app with servlets clash"
        TestSupport.runWebApp(ClashApp, null)
        then: "exception thrown"
        def ex = thrown(IllegalStateException)
        ex.message == "Servlet registration Servlet2 clash with already installed servlets on paths: /sam"
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