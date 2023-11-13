package ru.vyarus.dropwizard.guice.yaml.qualifier

import com.google.inject.CreationException
import com.google.inject.name.Named
import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.TestSupport
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 11.11.2023
 */
class BadQualifierDefinitionTest extends Specification {

    def 'Check duplicate qualification bindings'() {

        when: "run app with duplicate qualifiers"
        TestSupport.runCoreApp(App, 'src/test/resources/ru/vyarus/dropwizard/guice/config.yml')

        then: "error"
        def ex = thrown(CreationException)
        ex.message.replace("value=", "").contains("String annotated with @Named(\"one\") was bound multiple times")
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
        String foo
        @Named("one")
        String bar
        String baa
    }
}
