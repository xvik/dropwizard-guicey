package ru.vyarus.dropwizard.guice.web.async

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.installer.feature.web.AdminContext
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import spock.lang.Specification

import jakarta.servlet.AsyncContext
import jakarta.servlet.ServletException
import jakarta.servlet.annotation.WebServlet
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

/**
 * @author Vyacheslav Rusakov
 * @since 18.08.2016
 */
@TestDropwizardApp(AsyncServletApp)
class AsyncServletTest extends Specification {

    def "Check async servlet"() {

        expect: "servlet works"
        new URL("http://localhost:8080/async").getText() == 'done!'
        new URL("http://localhost:8081/async").getText() == 'done!'

    }

    static class AsyncServletApp extends Application<Configuration> {
        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .extensions(AsyncServlet)
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {

        }
    }

    @AdminContext(andMain = true)
    @WebServlet(urlPatterns = "/async", asyncSupported = true)
    static class AsyncServlet extends HttpServlet {

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            final String thread = Thread.currentThread().name
            final AsyncContext context = req.startAsync()
            context.start({
                assert thread != Thread.currentThread().name
                println "async servlet"
                context.getResponse().writer.write("done!")
                context.complete()
            })
        }
    }
}