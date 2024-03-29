package ru.vyarus.dropwizard.guice.provider

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.provider.JerseyProviderInstaller
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp

import jakarta.inject.Inject
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper

/**
 * @author Vyacheslav Rusakov
 * @since 29.11.2022
 */
@TestDropwizardApp(App)
class JerseyExtInstallByTypeTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info

    def "Check installation without provider annotation"() {

        expect: "extension installed"
        info.getExtensions(JerseyProviderInstaller).get(0) == ExM
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .extensions(ExM)
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
