package ru.vyarus.dropwizard.guice.config.unique

import ru.vyarus.dropwizard.guice.module.context.unique.item.UniqueGuiceyBundle
import ru.vyarus.dropwizard.guice.module.context.unique.item.UniqueModule
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 28.09.2019
 */
class UniqueItemsEqualsTest extends Specification {

    def "Check unique bundle equals"() {

        setup:
        def b1 = new Bundle()
        def b2 = new Bundle()

        expect:
        b1.equals(b2)
        b1.hashCode() == b2.hashCode()
        !b1.equals(null)
        !b2.equals(null)
        !b1.equals(new Module())
    }

    def "Check unique module equals"() {

        setup:
        def m1 = new Module()
        def m2 = new Module()

        expect:
        m1.equals(m2)
        m1.hashCode() == m2.hashCode()
        !m1.equals(null)
        !m2.equals(null)
        !m1.equals(new Bundle())
    }

    static class Bundle extends UniqueGuiceyBundle {}

    static class Module extends UniqueModule {}
}
