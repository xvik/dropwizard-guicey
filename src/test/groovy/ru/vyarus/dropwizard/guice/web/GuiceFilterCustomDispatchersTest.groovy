package ru.vyarus.dropwizard.guice.web

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import org.eclipse.jetty.servlet.FilterMapping
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.jersey.GuiceWebModule
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp
import spock.lang.Specification

import javax.inject.Inject

import static javax.servlet.DispatcherType.ERROR
import static javax.servlet.DispatcherType.FORWARD
import static javax.servlet.DispatcherType.REQUEST
import static ru.vyarus.dropwizard.guice.GuiceyOptions.GuiceFilterRegistration

/**
 * @author Vyacheslav Rusakov
 * @since 21.08.2016
 */
@UseGuiceyApp(CDApp)
class GuiceFilterCustomDispatchersTest extends Specification {

    @Inject
    Environment environment

    def "Check guice filter registration option"() {

        expect: "correct registration"
        FilterMapping gf = environment.getApplicationContext().getServletHandler().getFilterMappings().find {
            it.filterName == GuiceWebModule.GUICE_FILTER
        }
        gf.appliesTo(REQUEST)
        gf.appliesTo(FORWARD)
        !gf.appliesTo(ERROR)
    }

    static class CDApp extends Application<Configuration> {
        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .option(GuiceFilterRegistration, EnumSet.of(REQUEST, FORWARD))
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {

        }
    }
}