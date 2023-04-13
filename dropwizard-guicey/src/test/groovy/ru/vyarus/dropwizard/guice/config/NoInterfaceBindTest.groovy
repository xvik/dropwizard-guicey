package ru.vyarus.dropwizard.guice.config

import com.google.inject.Injector
import com.google.inject.Key
import io.dropwizard.core.Configuration
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.config.support.NoIfaceBindingApp
import ru.vyarus.dropwizard.guice.config.support.conf.*
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import spock.lang.Unroll

import jakarta.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 20.06.2016
 */
@TestGuiceyApp(NoIfaceBindingApp)
class NoInterfaceBindTest extends AbstractTest {

    @Inject
    Injector injector

    @Unroll
    def "Check configuration type #type.simpleName mapping"() {

        expect: "class hierarchy mapped"
        injector.getBinding(type)

        where:
        type          | _
        Configuration | _
        ConfigLevel2  | _
        ConfigLevel1  | _

    }

    @Unroll
    def "Check type #type.simpleName not mapped"() {

        expect: "interfaces not mapped"
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