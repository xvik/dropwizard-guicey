package ru.vyarus.dropwizard.guice.test.unit

import io.dropwizard.testing.ConfigOverride
import ru.vyarus.dropwizard.guice.test.util.ConfigOverrideUtils
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 02.05.2020
 */
class ConfigOverrideUtilsTest extends Specification {

    def "Check value conversion"() {

        expect:
        ConfigOverride[] res = ConfigOverrideUtils.convert(val)
        res[0].key == key
        res[0].value.get() == value

        where:
        val     | key | value
        'a=b'   | 'a' | 'b'
        'a=b=c' | 'a' | 'b=c'
    }

    def "Check error conversion cases"() {

        when: "no key"
        ConfigOverrideUtils.convert('=bbb')
        then: "err"
        thrown(IllegalStateException)

        when: "no ="
        ConfigOverrideUtils.convert('key-bbb')
        then: "err"
        thrown(IllegalStateException)
    }

    def "Check config merge"() {

        ConfigOverride[] base = null

        when: "add nothing to null"
        base = ConfigOverrideUtils.merge(base)
        then: "not changed"
        base == null

        when: "add something to null"
        base = ConfigOverrideUtils.merge(base, ConfigOverride.config("a", "b"))
        then: "addition returned"
        base.length == 1
        base[0].key == 'a'

        when: "add nothing not existing"
        base = ConfigOverrideUtils.merge(base)
        then: "not changed"
        base.length == 1
        base[0].key == 'a'

        when: "real merge"
        base = ConfigOverrideUtils.merge(base,  ConfigOverride.config("c", "c"),  ConfigOverride.config("d", "d"))
        then: "merged"
        base.length == 3
        base.collect { it.key } == ['a', 'c', 'd']
    }
}
