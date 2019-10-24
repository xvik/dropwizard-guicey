package ru.vyarus.dropwizard.guice.debug

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.debug.renderer.web.support.GuiceWebModule
import ru.vyarus.dropwizard.guice.debug.renderer.web.support.UserServletsBundle
import ru.vyarus.dropwizard.guice.test.spock.UseDropwizardApp

/**
 * @author Vyacheslav Rusakov
 * @since 22.10.2019
 */
@UseDropwizardApp(App)
class WebMappingsReportTest extends AbstractTest {

    def "Check web report"() {
        // actual reporting checked manually (test used for reporting configuration)

        expect: "check report doesn't fail"
        true
    }

    static class App extends Application<Configuration> {
        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(new UserServletsBundle())
                    .modules(new GuiceWebModule())
                    .printWebMappings()
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
