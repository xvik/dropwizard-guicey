package ru.vyarus.dropwizard.guice.guiceconfig

import com.google.inject.*
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.installer.feature.LifeCycleInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.ManagedInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.TaskInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.eager.EagerSingletonInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.health.HealthCheckInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.JerseyFeatureInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.ResourceInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.provider.JerseyProviderInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.plugin.PluginInstaller
import ru.vyarus.dropwizard.guice.module.jersey.debug.service.HK2DebugFeature
import ru.vyarus.dropwizard.guice.support.feature.*
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp

/**
 * @author Vyacheslav Rusakov
 * @since 30.08.2019
 */
@TestGuiceyApp(App)
class ExtensionBindingsTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info
    @Inject
    Injector injector

    def "Check binding configuration"() {

        when: "application started"
        then: "task found"
        info.getExtensions(TaskInstaller) == [DummyTask]

        then: "resource found"
        info.getExtensions(ResourceInstaller) == [DummyResource]

        then: "managed found"
        info.getExtensions(ManagedInstaller) == [DummyManaged]

        then: "lifecycle found"
        info.getExtensions(LifeCycleInstaller) == [DummyLifeCycle]

        then: "jersey provider found"
        info.getExtensions(JerseyProviderInstaller) as Set == [DummyExceptionMapper, DummyJerseyProvider, DummyOtherProvider] as Set

        then: "feature found"
        info.getExtensions(JerseyFeatureInstaller) as Set == [DummyFeature, HK2DebugFeature] as Set

        then: "health check found"
        info.getExtensions(HealthCheckInstaller) == [DummyHealthCheck]

        then: "eager found"
        info.getExtensions(EagerSingletonInstaller) == [DummyService]

        then: "plugins found"
        info.getExtensions(PluginInstaller) as Set == [DummyPlugin1, DummyPlugin2, DummyNamedPlugin1, DummyNamedPlugin2] as Set
        injector.getInstance(Key.get(new TypeLiteral<Set<PluginInterface>>() {})).size() == 2
        injector.getInstance(Key.get(new TypeLiteral<Map<DummyPluginKey, PluginInterface>>() {})).size() == 2

    }

    static class App extends Application<Configuration> {
        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .modules(new ExtsModule())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {

        }
    }

    static class ExtsModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(DummyTask)
            bind(DummyResource)
            bind(DummyManaged)
            bind(DummyLifeCycle)
            bind(DummyExceptionMapper)
            bind(DummyJerseyProvider)
            bind(DummyOtherProvider)
            bind(DummyFeature)
            bind(DummyHealthCheck)
            bind(DummyPlugin1)
            bind(DummyPlugin2)
            bind(DummyNamedPlugin1)
            bind(DummyNamedPlugin2)
            bind(DummyService).asEagerSingleton()
        }
    }
}
