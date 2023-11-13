package ru.vyarus.dropwizard.guice.yaml.qualifier

import com.google.inject.Inject
import com.google.inject.name.Named
import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
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
@TestGuiceyApp(value = App, configOverride = ["one1.val:1", "one2.val:2", "two.val:3"])
class QualifiedAggregationTest extends Specification {

    @Inject
    @Named("one")
    Set<Sub> subs

    @Inject
    @Named("two")
    Sub two

    @Inject
    ConfigurationTree tree

    def "Check grouped bindings"() {

        expect:
        subs.size() == 2
        two.val == 3
        and: "report correct"
        render(new BindingsConfig()
                .showCustomConfigOnly()) == """

    Configuration object bindings:
        @Config Config


    Qualified bindings:
        @Named("one") Set<Sub> = (aggregated values)
            Sub = ru.vyarus.dropwizard.guice.yaml.qualifier.QualifiedAggregationTest\$Sub@1111111 (one1)
            Sub = ru.vyarus.dropwizard.guice.yaml.qualifier.QualifiedAggregationTest\$Sub@1111111 (one2)
        @Named("two") Sub = ru.vyarus.dropwizard.guice.yaml.qualifier.QualifiedAggregationTest\$Sub@1111111 (two)


    Configuration paths bindings:

        Config:
            @Config("one1") Sub = ru.vyarus.dropwizard.guice.yaml.qualifier.QualifiedAggregationTest\$Sub@1111111
            @Config("one1.val") Integer = 1
            @Config("one2") Sub = ru.vyarus.dropwizard.guice.yaml.qualifier.QualifiedAggregationTest\$Sub@1111111
            @Config("one2.val") Integer = 2
            @Config("two") Sub = ru.vyarus.dropwizard.guice.yaml.qualifier.QualifiedAggregationTest\$Sub@1111111
            @Config("two.val") Integer = 3
"""
    }

    String render(BindingsConfig config) {
        new ConfigBindingsRenderer(tree).renderReport(config)
                .replaceAll("\r", "")
                .replaceAll(" +\n", "\n")
                .replaceAll('@\\d+[^]C \n]+', '@1111111')
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
        @Named("one")
        Sub one1 = new Sub()
        @Named("one")
        Sub one2 = new Sub()
        @Named("two")
        Sub two = new Sub()
    }

    static class Sub {
        Integer val
    }
}
