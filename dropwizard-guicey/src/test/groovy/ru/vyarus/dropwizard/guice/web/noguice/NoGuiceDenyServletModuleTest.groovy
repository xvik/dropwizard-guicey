package ru.vyarus.dropwizard.guice.web.noguice

import com.google.inject.CreationException
import com.google.inject.servlet.ServletModule
import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.TestSupport
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 21.08.2016
 */
class NoGuiceDenyServletModuleTest extends Specification {

    def "Check servlet module denied without guice filter"() {

        when: "start app with servlet module and no filter"
        TestSupport.runWebApp(DenySMApp)
        then: "error"
        def ex = thrown(CreationException)
        ex.errorMessages[0].message.equals("jakarta.servlet.http.HttpServletRequest was bound multiple times.")
    }

    static class DenySMApp extends Application<Configuration> {
        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .noGuiceFilter()
                    .modules(new ServletModule())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {

        }
    }
}