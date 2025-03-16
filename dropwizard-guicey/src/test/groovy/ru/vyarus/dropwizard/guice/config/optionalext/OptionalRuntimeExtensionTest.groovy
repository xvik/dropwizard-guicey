package ru.vyarus.dropwizard.guice.config.optionalext

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import jakarta.inject.Inject
import jakarta.ws.rs.Path
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyEnvironment
import ru.vyarus.dropwizard.guice.module.installer.feature.eager.EagerSingleton
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.ResourceInstaller
import ru.vyarus.dropwizard.guice.module.jersey.debug.service.HK2DebugFeature
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp

/**
 * @author Vyacheslav Rusakov
 * @since 14.03.2025
 */
@TestGuiceyApp(App)
class OptionalRuntimeExtensionTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info

    def "Check optional extensions support"() {

        expect: "one extension accepted"
        info.getExtensions() == [ExtAccepted, HK2DebugFeature, ExtAccepted2]

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
        void run(GuiceyEnvironment environment) throws Exception {
            environment.extensionsOptional(
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
