package ru.vyarus.dropwizard.guice.web.async

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.installer.feature.web.AdminContext
import ru.vyarus.dropwizard.guice.test.spock.UseDropwizardApp
import spock.lang.Specification

import javax.servlet.*
import javax.servlet.annotation.WebFilter

/**
 * @author Vyacheslav Rusakov
 * @since 18.08.2016
 */
@UseDropwizardApp(AsyncFilterApp)
class AsyncFilterTest extends Specification {

    def "Check async filter"() {

        expect: "filter works"
        new URL("http://localhost:8080/asyncf").getText() == 'done!'
        new URL("http://localhost:8081/asyncf").getText() == 'done!'

    }

    static class AsyncFilterApp extends Application<Configuration> {
        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .useWebInstallers()
                    .extensions(AsyncFilter)
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {

        }
    }

    @AdminContext(andMain = true)
    @WebFilter(urlPatterns = "/asyncf", asyncSupported = true)
    static class AsyncFilter implements Filter {

        @Override
        void init(FilterConfig filterConfig) throws ServletException {
        }

        @Override
        void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            final String thread = Thread.currentThread().name
            final AsyncContext context = request.startAsync()
            context.start({
                Thread.sleep(200)
                if (thread != Thread.currentThread().name) {
                    context.getResponse().writer.write("done!")
                } else {
                    context.getResponse().writer.write("ERROR: async executed at the same thread " + thread)
                }
                println "async filter"

                context.complete()
            })
        }

        @Override
        void destroy() {
        }
    }
}