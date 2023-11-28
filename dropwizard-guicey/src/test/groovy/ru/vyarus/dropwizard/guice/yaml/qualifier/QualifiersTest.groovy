package ru.vyarus.dropwizard.guice.yaml.qualifier

import com.google.inject.BindingAnnotation
import com.google.inject.Inject
import com.google.inject.name.Named
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.debug.report.yaml.BindingsConfig
import ru.vyarus.dropwizard.guice.debug.report.yaml.ConfigBindingsRenderer
import ru.vyarus.dropwizard.guice.module.yaml.ConfigurationTree
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import spock.lang.Specification

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * @author Vyacheslav Rusakov
 * @since 11.11.2023
 */
@TestGuiceyApp(value = App, configOverride = ["one:1", "sub.two:2", "three:3", "box.foo:4"])
class QualifiersTest extends Specification {

    @Named("one")
    @Inject
    String one

    @Qualif
    @Inject
    Integer two

    @Qualif
    @Inject
    Box box

    @Named("custom")
    @Inject
    String custom

    @Inject
    ConfigurationTree tree

    def 'Check qualification bindings'() {

        expect: "qualified bindings recognized"
        one == "1"
        two == 2
        box.foo == 4
        custom == "3"

        and: "report correct"
        render(new BindingsConfig()
                .showCustomConfigOnly()) == """

    Configuration object bindings:
        @Config Config


    Unique sub configuration objects bindings:

        Config.box
            @Config Box = ru.vyarus.dropwizard.guice.yaml.qualifier.QualifiersTest\$Box@1111111

        Config.sub
            @Config Sub = ru.vyarus.dropwizard.guice.yaml.qualifier.QualifiersTest\$Sub@1111111


    Qualified bindings:
        @Qualif Box = ru.vyarus.dropwizard.guice.yaml.qualifier.QualifiersTest\$Box@1111111 (box)
        @Named("one") String = "1" (one)
        @Qualif Integer = 2 (sub.two)
        @Named("custom") String = "3" (three)


    Configuration paths bindings:

        Config:
            @Config("box") Box = ru.vyarus.dropwizard.guice.yaml.qualifier.QualifiersTest\$Box@1111111
            @Config("box.foo") Integer = 4
            @Config("one") String = "1"
            @Config("sub") Sub = ru.vyarus.dropwizard.guice.yaml.qualifier.QualifiersTest\$Sub@1111111
            @Config("sub.two") Integer = 2
            @Config("three") String = "3"
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
        @Named("one")
        String one
        Sub sub = new Sub()
        @Qualif Box box = new Box()

        String three

        @Named("custom")
        String getThree() {
            return three
        }
    }

    static class Sub {
        @Qualif
        Integer two
    }

    static class Box {
        Integer foo
    }

}

@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD])
@BindingAnnotation
public @interface Qualif {
}
