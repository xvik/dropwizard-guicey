package ru.vyarus.dropwizard.guice.provider

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.provider.JerseyProviderInstaller
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp

import javax.inject.Inject
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper

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
