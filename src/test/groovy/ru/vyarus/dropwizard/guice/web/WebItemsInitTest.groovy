package ru.vyarus.dropwizard.guice.web

import com.google.inject.Inject
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import org.eclipse.jetty.servlet.FilterHolder
import org.eclipse.jetty.servlet.FilterMapping
import org.eclipse.jetty.servlet.ServletHolder
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.support.web.params.InitParamsFilter
import ru.vyarus.dropwizard.guice.support.web.params.InitParamsServlet
import ru.vyarus.dropwizard.guice.support.web.params.ServletRegFilter
import ru.vyarus.dropwizard.guice.test.spock.UseDropwizardApp

import javax.servlet.DispatcherType

/**
 * @author Vyacheslav Rusakov
 * @since 08.08.2016
 */
@UseDropwizardApp(WebInitApp)
class WebItemsInitTest extends AbstractTest {

    @Inject
    Environment environment

    def "Check servlets and filters configuration"() {

        expect: "servlet configured"
        ServletHolder servlet = environment.getApplicationContext().getServletHandler().getServlet("sample")
        servlet.registration.className == InitParamsServlet.name
        servlet.registration.mappings == ['/sample']
        servlet.asyncSupported

        and: "filter configured"
        FilterHolder filter = environment.getApplicationContext().getServletHandler().getFilter("dummy")
        filter.registration.className == InitParamsFilter.name
        filter.registration.urlPatternMappings == ['/dummy']
        filter.asyncSupported

        and: "servlet filter configured"
        FilterHolder filter2 = environment.getApplicationContext().getServletHandler().getFilter("samsam")
        filter2.registration.className == ServletRegFilter.name
        filter2.registration.servletNameMappings == ['samsam']
        !filter2.asyncSupported
        FilterMapping mapping = environment.getApplicationContext().getServletHandler().getFilterMappings().find {
            it.filterName == 'samsam'
        }
        mapping.appliesTo(DispatcherType.ERROR)
    }

    static class WebInitApp extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .enableAutoConfig(InitParamsFilter.package.name)
                    .useWebInstallers()
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
