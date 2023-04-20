package ru.vyarus.dropwizard.guice.unit

import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
import ru.vyarus.dropwizard.guice.module.jersey.support.GuiceComponentFactory
import ru.vyarus.dropwizard.guice.module.jersey.support.JerseyComponentProvider
import ru.vyarus.dropwizard.guice.module.jersey.support.LazyGuiceFactory
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 12.09.2019
 */
class JesreySuppliersToStringTest extends Specification {

    def "Check factories to string"() {

        expect:
        new GuiceComponentFactory<>(null, GuiceyBundle).toString() == "GuiceComponentFactory for GuiceyBundle"
        new LazyGuiceFactory<>(null, GuiceyBundle).toString() == "LazyGuiceFactory for GuiceyBundle"
        new JerseyComponentProvider<>(null, GuiceyBundle).toString() == "JerseyComponentProvider for GuiceyBundle"
    }
}
