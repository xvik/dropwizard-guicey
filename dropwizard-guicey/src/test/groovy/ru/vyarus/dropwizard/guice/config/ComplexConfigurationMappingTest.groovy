package ru.vyarus.dropwizard.guice.config

import com.google.inject.Injector
import com.google.inject.Key
import io.dropwizard.core.Configuration
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.config.support.ComplexConfigApp
import ru.vyarus.dropwizard.guice.config.support.conf.*
import ru.vyarus.dropwizard.guice.module.yaml.bind.Config
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import spock.lang.Unroll

import jakarta.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 19.06.2016
 */
@TestGuiceyApp(ComplexConfigApp)
class ComplexConfigurationMappingTest extends AbstractTest {

    @Inject
    Injector injector

    @Unroll
    def "Check configuration type #type mapping"() {

        expect: "all required mapped"
        injector.getBinding(type)

        where:
        type                             | _
        Configuration                    | _
        ConfigLevel2                     | _
        ConfigLevel1                     | _
        Key.get(Level2Interface, Config) | _
        Key.get(Level1Interface, Config) | _
    }

    @Unroll
    def "Check type #type.simpleName not mapped"() {

        expect: "not mapped"
        !injector.getExistingBinding(Key.get(type))

        where:
        type                    | _
        Level2Interface         | _
        Level1Interface         | _
        Level1IndirectInterface | _
        Serializable            | _
        Cloneable               | _
    }
}