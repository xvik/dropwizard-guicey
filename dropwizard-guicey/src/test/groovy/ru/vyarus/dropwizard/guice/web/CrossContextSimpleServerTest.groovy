package ru.vyarus.dropwizard.guice.web

import com.google.inject.Inject
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.support.web.crosscontext.CrossContextListener
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp

/**
 * @author Vyacheslav Rusakov
 * @since 09.08.2016
 */
@TestDropwizardApp(value = CrossContextTest.CrossApp,
        config = 'src/test/resources/ru/vyarus/dropwizard/guice/simple-server.yml')
class CrossContextSimpleServerTest extends AbstractTest {

    @Inject
    Environment environment

    def "Check multiple contexts registration"() {

        def main = environment.getApplicationContext().getServletHandler()
        def admin = environment.getAdminContext().getServletHandler()
        CrossContextListener.contexts.clear()

        expect: "filter registered"
        main.getFilter(".crosscontext")
        admin.getFilter(".crosscontext")
        new URL("http://localhost:8080/crossF").getText() == "ok"
        new URL("http://localhost:8080/admin/crossF").getText() == "ok"

        and: "servlet registered"
        main.getServlet(".crosscontext")
        admin.getServlet(".crosscontext")
        new URL("http://localhost:8080/crossS").getText() == "ok"
        new URL("http://localhost:8080/admin/crossS").getText() == "ok"

        and: "listener registered and called for both contexts"
        CrossContextListener.contexts as Set == ['/', '/admin'] as Set

    }
}
