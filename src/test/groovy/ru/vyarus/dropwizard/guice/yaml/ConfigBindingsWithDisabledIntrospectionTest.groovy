package ru.vyarus.dropwizard.guice.yaml

import com.google.inject.Binding
import com.google.inject.Injector
import com.google.inject.Key
import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.logging.common.LoggingFactory
import io.dropwizard.core.server.ServerFactory
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.GuiceyOptions
import ru.vyarus.dropwizard.guice.module.yaml.ConfigurationTree
import ru.vyarus.dropwizard.guice.module.yaml.bind.Config
import ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigImpl
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 23.07.2018
 */
@TestGuiceyApp(App)
class ConfigBindingsWithDisabledIntrospectionTest extends AbstractTest {

    @Inject
    Injector injector

    def "Check bindings"() {

        expect: "root bindings"
        binding(ConfigurationTree) != null
        binding(Configuration) != null

        and: "root qualified bindings"
        annBinding(Configuration) != null

        and: "unique objects not bound"
        annBinding(ServerFactory) == null
        annBinding(LoggingFactory) == null

        and: "path bindings not set"
        pathBinding(Integer, "server") == null
        pathBinding(Integer, "server.adminMaxThreads") == null
    }

    private Binding binding(Class type) {
        injector.getExistingBinding(Key.get(type))
    }

    private Binding annBinding(Class type) {
        injector.getExistingBinding(Key.get(type, Config))
    }

    private Binding pathBinding(Class type, String path) {
        injector.getExistingBinding(Key.get(type, new ConfigImpl(path)))
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .option(GuiceyOptions.BindConfigurationByPath, false)
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
