package ru.vyarus.dropwizard.guice.yaml

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.yaml.ConfigTreeBuilder
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp

import jakarta.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 23.05.2020
 */
@TestGuiceyApp(App)
class NestedMapsConfigTest extends AbstractTest {

    @Inject
    Bootstrap bootstrap

    def "Check configuration mapping"() {

        when: "analyse config"
        def cfg = ConfigTreeBuilder.build(bootstrap, create(Config))
        then: "parsed"
        cfg.findByPath('simpleMap').toStringDeclaredType() == 'Map<String, Map<String, String>>'
        cfg.findByPath('mapWithEnum').toStringDeclaredType() == 'Map<Enum1, Map<Enum2, String>>'
        // not enum map because ALWAYS collection type is used instead of implementation
        cfg.findByPath('enumMap').toStringDeclaredType() == 'Map<Enum1, EnumMap<Enum2, String>>'

        cfg.findByPath('simpleMap').toStringType() == 'LinkedHashMap<String, Map<String, String>>'
        cfg.findByPath('mapWithEnum').toStringType() == 'LinkedHashMap<Enum1, Map<Enum2, String>>'
        cfg.findByPath('enumMap').toStringType() == 'EnumMap<Enum1, EnumMap<Enum2, String>>'
    }

    private <T extends Configuration> T create(Class<T> type) {
        bootstrap.configurationFactoryFactory
                .create(type, bootstrap.validatorFactory.validator, bootstrap.objectMapper, "dw").build()
    }

    enum Enum1 {
        ONE, TWO
    }

    enum Enum2 {
        THREE, FOUR
    }

    static class Config extends Configuration {
        Map<String, Map<String, String>> simpleMap = new HashMap<>()
        Map<Enum1, Map<Enum2, String>> mapWithEnum = new HashMap<>()
        EnumMap<Enum1, EnumMap<Enum2, String>> enumMap = new EnumMap<>(Enum1)
    }

    static class App extends Application<Config> {

        @Override
        void initialize(Bootstrap<Config> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .printCustomConfigurationBindings()
                    .build())
        }

        @Override
        void run(Config configuration, Environment environment) throws Exception {
        }
    }
}
