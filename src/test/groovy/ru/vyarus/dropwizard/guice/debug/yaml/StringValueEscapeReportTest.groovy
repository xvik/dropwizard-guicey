package ru.vyarus.dropwizard.guice.debug.yaml

import io.dropwizard.core.Application
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.debug.report.yaml.BindingsConfig
import ru.vyarus.dropwizard.guice.debug.report.yaml.ConfigBindingsRenderer
import ru.vyarus.dropwizard.guice.module.yaml.ConfigurationTree
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import ru.vyarus.dropwizard.guice.yaml.support.SimpleConfig
import spock.lang.Specification

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 13.03.2020
 */
@TestGuiceyApp(value = App, configOverride = ["foo: string %s with %d params"])
class StringValueEscapeReportTest extends Specification {

    @Inject
    ConfigurationTree tree

    def "Check value with parameters report"() {
        expect:
        render(new BindingsConfig()
                .showCustomConfigOnly()
                .showConfigurationTree()) == """

    SimpleConfig (visible paths)
    ├── foo: String = "string %s with %d params"
    └── prim: Integer = 0


    Configuration object bindings:
        @Config SimpleConfig


    Configuration paths bindings:

        SimpleConfig:
            @Config("foo") String = "string %s with %d params"
            @Config("prim") Integer = 0
"""
    }

    String render(BindingsConfig config) {
        new ConfigBindingsRenderer(tree).renderReport(config)
                .replaceAll("\r", "")
                .replaceAll(" +\n", "\n")
                .replaceAll('@[^]C \n]+', '@1111111')
    }

    static class App extends Application<SimpleConfig> {
        @Override
        void initialize(Bootstrap<SimpleConfig> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder().build())
        }

        @Override
        void run(SimpleConfig configuration, Environment environment) throws Exception {
        }
    }
}
