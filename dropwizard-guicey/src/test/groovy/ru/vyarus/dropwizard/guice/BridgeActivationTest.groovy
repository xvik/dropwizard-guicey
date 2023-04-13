package ru.vyarus.dropwizard.guice

import com.google.inject.Injector
import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.debug.ConfigurationDiagnostic
import ru.vyarus.dropwizard.guice.debug.report.option.OptionsConfig
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.JerseyManaged
import ru.vyarus.dropwizard.guice.test.TestSupport
import spock.lang.Specification

import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path

/**
 * @author Vyacheslav Rusakov
 * @since 26.03.2017
 */
class BridgeActivationTest extends Specification {

    def "Check startup fail without bridge"() {

        when: "start app without server"
        App.enableBridge = false
        TestSupport.runWebApp(App, null, {
            // force resource creation
            new URL('http://localhost:8080/sample/').text
        })

        then: "failed"
        def ex = thrown(IOException)
        ex.message.contains('Server returned HTTP response code: 500')
    }

    def "Check startup ok with bridge"() {

        when: "start with bridge enabled"
        App.enableBridge = true
        String res = TestSupport.runWebApp(App, null, {
             new URL('http://localhost:8080/sample/').text
        })

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
                    .listen(ConfigurationDiagnostic.builder()
                            .printOptions(new OptionsConfig())
                            .build())
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