package ru.vyarus.dropwizard.guice.web

import com.google.inject.Inject
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.support.web.crosscontext.CrossContextFilter
import ru.vyarus.dropwizard.guice.support.web.crosscontext.CrossContextListener
import ru.vyarus.dropwizard.guice.support.web.crosscontext.CrossContextServlet
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp

/**
 * @author Vyacheslav Rusakov
 * @since 08.08.2016
 */
@TestDropwizardApp(CrossApp)
class CrossContextTest extends AbstractTest {

    @Inject
    Environment environment

    def "Check multiple contexts registration"() {

        def main = environment.getApplicationContext().getServletHandler()
        def admin = environment.getAdminContext().getServletHandler()

        expect: "filter registered"
        main.getFilter(".crosscontext")
        admin.getFilter(".crosscontext")
        new URL("http://localhost:8080/crossF").getText() == "ok"
        new URL("http://localhost:8081/crossF").getText() == "ok"

        and: "servlet registered"
        main.getServlet(".crosscontext")
        admin.getServlet(".crosscontext")
        new URL("http://localhost:8080/crossS").getText() == "ok"
        new URL("http://localhost:8081/crossS").getText() == "ok"

        and: "listener registered and called for both contexts"
        CrossContextListener.ports as Set == [8080, 8081] as Set

    }

    static class CrossApp extends Application<Configuration> {
        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .extensions(CrossContextFilter, CrossContextServlet, CrossContextListener)
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {

        }
    }
}
