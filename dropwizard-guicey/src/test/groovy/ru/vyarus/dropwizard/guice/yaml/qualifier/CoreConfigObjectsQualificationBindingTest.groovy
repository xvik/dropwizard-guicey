package ru.vyarus.dropwizard.guice.yaml.qualifier

import com.google.inject.Inject
import com.google.inject.name.Named
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import io.dropwizard.metrics.MetricsFactory
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.debug.report.yaml.BindingsConfig
import ru.vyarus.dropwizard.guice.debug.report.yaml.ConfigBindingsRenderer
import ru.vyarus.dropwizard.guice.module.yaml.ConfigurationTree
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 13.11.2023
 */
@TestGuiceyApp(App)
class CoreConfigObjectsQualificationBindingTest extends Specification {

    @Inject
    @Named("metrics")
    MetricsFactory metrics

    @Inject
    Config config

    @Inject
    ConfigurationTree tree

    def 'Check qualification bindings'() {

        expect: "qualified bindings recognized"
        metrics == config.getMetricsFactory()

        and: "report correct"
        render(new BindingsConfig()
                .showCustomConfigOnly()) == """

    Configuration object bindings:
        @Config Config


    Unique sub configuration objects bindings:

        Config.metrics
            @Config MetricsFactory = MetricsFactory{frequency=1 minute, reporters=[], reportOnStop=false}


    Qualified bindings:
        @Named("metrics") MetricsFactory = MetricsFactory{frequency=1 minute, reporters=[], reportOnStop=false} (metrics)


    Configuration paths bindings:

        Config:
            @Config("metrics") MetricsFactory = MetricsFactory{frequency=1 minute, reporters=[], reportOnStop=false}
            @Config("metrics.frequency") Duration = 1 minute
            @Config("metrics.reportOnStop") Boolean = false
            @Config("metrics.reporters") List<ReporterFactory> (with actual type ArrayList<ReporterFactory>) = []
"""
    }

    String render(BindingsConfig config) {
        new ConfigBindingsRenderer(tree).renderReport(config)
                .replaceAll("\r", "")
                .replaceAll(" +\n", "\n")
                .replaceAll('@(\\d+|[a-z])[^]C \n]+', '@1111111')
    }


    static class App extends Application<Config> {
        @Override
        void initialize(Bootstrap<Config> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .printCustomConfigurationBindings()
                    .build())
        }

        @Override
        void run(Config config, Environment environment) throws Exception {
        }
    }

    static class Config extends Configuration {

        @Named("metrics")
        @Override
        MetricsFactory getMetricsFactory() {
            return super.getMetricsFactory()
        }
    }
}
