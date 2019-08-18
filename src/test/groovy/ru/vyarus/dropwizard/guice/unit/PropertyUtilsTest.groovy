package ru.vyarus.dropwizard.guice.unit

import ru.vyarus.dropwizard.guice.module.installer.util.PropertyUtils
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 18.08.2019
 */
class PropertyUtilsTest extends Specification {

    def "Check property set"() {

        when: "set single clas"
        PropertyUtils.setProperty("test", Foo)
        then: "property set"
        System.getProperty("test") == Foo.name

        when: "set multiple properties"
        PropertyUtils.setProperty("test", Foo, Bar)
        then: "property set"
        System.getProperty("test") == "$Foo.name,$Bar.name"
    }

    def "Check property get"() {

        when: "one item property"
        PropertyUtils.setProperty("test", Foo)
        def res = PropertyUtils.getProperty("test", [:])
        then: "one instance returned"
        res.size() == 1
        res[0] instanceof Foo


        when: "multiple item properties"
        PropertyUtils.setProperty("test", Foo, Bar)
        res = PropertyUtils.getProperty("test", [:])
        then: "one instance returned"
        res.size() == 2
        res[0] instanceof Foo
        res[1] instanceof Bar
    }

    def "Check aliases support"() {

        when: "declare property with aliases"
        System.setProperty("test", "foo,bar")
        def res = PropertyUtils.getProperty("test", ['foo': Foo.name, 'bar': Bar.name])
        then: "aliases correctly resolved"
        res[0] instanceof Foo
        res[1] instanceof Bar

        when: "mixed resolution"
        System.setProperty("test", "foo,$Bar.name")
        res = PropertyUtils.getProperty("test", ['foo': Foo.name])
        then: "aliases correctly resolved"
        res[0] instanceof Foo
        res[1] instanceof Bar
    }

    static class Foo {}

    static class Bar {}
}
