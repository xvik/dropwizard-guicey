package ru.vyarus.dropwizard.guice.web.noguice

import com.google.inject.CreationException
import com.google.inject.servlet.ServletModule
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import io.dropwizard.testing.junit.DropwizardAppRule
import ru.vyarus.dropwizard.guice.GuiceBundle
import spock.lang.Specification


/**
 * @author Vyacheslav Rusakov
 * @since 21.08.2016
 */
class NoGuiceDenyServletModuleTest extends Specification {

    def "Check servlet module denied without guice filter"() {

        when: "start app with servlet module and no filter"
        new DropwizardAppRule(DenySMApp).before()
        then: "error"
        def ex = thrown(CreationException)
        ex.errorMessages[0].message.startsWith("A binding to javax.servlet.http.HttpServletRequest was already configured ")
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