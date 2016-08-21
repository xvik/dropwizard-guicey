package ru.vyarus.dropwizard.guice.web

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import org.eclipse.jetty.servlet.ServletHolder
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp
import spock.lang.Specification

import javax.inject.Inject
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet


/**
 * @author Vyacheslav Rusakov
 * @since 22.08.2016
 */
@UseGuiceyApp(GCollApp)
class ServletGenNameCollisionTest extends Specification {

    @Inject
    Environment environment

    def "Check servlet name generation"() {

        expect: "OServlet name without postfix cut"
        ServletHolder oservlet = environment.getApplicationContext().getServletHandler().getServlet(".oservlet")
        oservlet.registration.className == OServlet.name

        and: "Servlet name without postfix cut"
        ServletHolder servlet = environment.getApplicationContext().getServletHandler().getServlet(".servlet")
        servlet.registration.className == Servlet.name
    }

    static class GCollApp extends Application<Configuration> {
        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .useWebInstallers()
                    .extensions(OServlet, Servlet)
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {

        }
    }

    @WebServlet("/foo")
    static class OServlet extends HttpServlet {}

    @WebServlet("/bar")
    static class Servlet extends HttpServlet {}
}