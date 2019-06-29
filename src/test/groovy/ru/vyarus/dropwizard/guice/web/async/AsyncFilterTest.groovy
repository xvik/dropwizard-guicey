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
        execute("http://localhost:8080/asyncf").startsWith('done!')
        execute("http://localhost:8081/asyncf").startsWith('done!')

    }

    private String execute(String url) {
        def res = new URL(url).getText()
        if (res != 'done!') {
            System.err.println(res)
        }
        return res
    }

    static class AsyncFilterApp extends Application<Configuration> {
        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
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
                String msg = "done!"
                // on appveyour (windows build) new thread could be not spawned
                if (thread == Thread.currentThread().name) {
                    msg += " ERROR: async executed at the same thread " + thread
                }
                println "async filter"
                context.getResponse().writer.write(msg)

                context.complete()
            })
        }

        @Override
        void destroy() {
        }
    }
}