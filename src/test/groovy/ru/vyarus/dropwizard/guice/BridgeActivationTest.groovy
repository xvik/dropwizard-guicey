package ru.vyarus.dropwizard.guice

import com.google.inject.Injector
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import io.dropwizard.testing.junit.DropwizardAppRule
import org.junit.Rule
import ru.vyarus.dropwizard.guice.module.context.debug.DiagnosticBundle
import ru.vyarus.dropwizard.guice.module.context.debug.report.option.OptionsConfig
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.JerseyManaged
import spock.lang.Specification

import javax.inject.Inject
import javax.ws.rs.GET
import javax.ws.rs.Path

/**
 * @author Vyacheslav Rusakov
 * @since 26.03.2017
 */
class BridgeActivationTest extends Specification {

    @Rule
    DropwizardAppRule rule = new DropwizardAppRule(App)

    void setup() {
        // rule will work first, so it's too late for the first method but affects second one
        App.enableBridge = true
    }

    def "Check startup fail without bridge"() {

        when: "start app without server"
        // force resource creation
        new URL('http://localhost:8080/sample/').text

        then: "failed"
        def ex = thrown(IOException)
        ex.message.contains('Server returned HTTP response code: 500')
    }

    def "Check startup ok with bridge"() {

        when: "start with bridge enabled"
        String res = new URL('http://localhost:8080/sample/').text

        then: "passed"
        res == 'ok'
    }

    static class App extends Application<Configuration> {

        static boolean enableBridge

        static void main(String[] args) {
            new App().run(args)
        }

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(DiagnosticBundle.builder().printOptions(new OptionsConfig()).build())
                    .extensions(HkService)
                    .option(GuiceyOptions.UseHkBridge, enableBridge)
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {

        }
    }

    @Path("/sample/")
    @JerseyManaged
    static class HkService {

        Injector injector

        @Inject
        HkService(Injector injector) {
            println "CREATION"
            this.injector = injector
        }

        @GET
        String get() {
            return "ok"
        }
    }
}