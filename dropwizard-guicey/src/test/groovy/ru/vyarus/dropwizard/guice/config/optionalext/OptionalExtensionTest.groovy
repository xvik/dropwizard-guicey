package ru.vyarus.dropwizard.guice.config.optionalext

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
import ru.vyarus.dropwizard.guice.module.installer.feature.eager.EagerSingleton
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.ResourceInstaller
import ru.vyarus.dropwizard.guice.module.jersey.debug.service.HK2DebugFeature
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp

import javax.inject.Inject
import javax.ws.rs.Path

/**
 * @author Vyacheslav Rusakov
 * @since 11.12.2019
 */
@TestGuiceyApp(App)
class OptionalExtensionTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info

    def "Check optional extensions support"() {

        expect: "one extension accepted"
        info.getExtensions() == [ExtAccepted, ExtAccepted2, HK2DebugFeature]

        and: "other disabled automatically"
        info.getExtensionsDisabled() == [ExtDisabled, ExtDisabled2]
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .noDefaultInstallers()
                    .installers(ResourceInstaller)
                    .extensionsOptional(ExtAccepted, ExtDisabled)
                    .bundles(new Bundle())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {

        }
    }

    static class Bundle implements GuiceyBundle {

        @Override
        void initialize(GuiceyBootstrap bootstrap) {
            bootstrap.extensionsOptional(
                    ExtAccepted2, ExtDisabled2
            )
        }
    }

    @Path("/1")
    static class ExtAccepted {}

    @Path("/2")
    static class ExtAccepted2 {}

    @EagerSingleton
    static class ExtDisabled {}

    @EagerSingleton
    static class ExtDisabled2 {}
}
