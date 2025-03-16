package ru.vyarus.dropwizard.guice.provider

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.installer.InstallersOptions

import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import ru.vyarus.dropwizard.guice.support.DefaultTestApp

/**
 * @author Vyacheslav Rusakov
 * @since 29.11.2022
 */
class JerseyExtLegacyBehaviourTest extends AbstractTest {

    def "Check installation without provider annotation"() {

        when: "starting"
        new App().run("server")

        then: "extension denied"
        def ex = thrown(RuntimeException)
        ex.cause.message.startsWith('No installer found for extension ru.vyarus.dropwizard.guice.provider.JerseyExtLegacyBehaviourTest$ExM.')
    }

    static class App extends DefaultTestApp {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .extensions(ExM)
                    .option(InstallersOptions.JerseyExtensionsRecognizedByType, false)
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    static class ExM implements ExceptionMapper<Throwable> {

        @Override
        Response toResponse(Throwable throwable) {
            return null
        }
    }
}
