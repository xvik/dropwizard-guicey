package ru.vyarus.dropwizard.guice.debug.util


import spock.lang.Specification

/**
 * Copy of TargetLengthBasedClassNameAbbreviator tests
 * https://github.com/qos-ch/logback/blob/master/logback-classic/src/test/java/ch/qos/logback/classic/pattern/TargetLengthBasedClassNameAbbreviatorTest.java
 *
 * @author Vyacheslav Rusakov
 * @since 29.09.2020
 */
class ClassNameAbbreviatorTest extends Specification {

    def "Check cases"() {
        expect:
        res == new ClassNameAbbreviator(size).abbreviate(input)

        where:
        size | input                                        | res
        100  | 'hello'                                      | 'hello'
        100  | 'hello.world'                                | 'hello.world'

        1    | 'hello'                                      | 'hello'
        1    | 'hello.world'                                | 'h.world'
        1    | 'h.world'                                    | 'h.world'
        1    | '.world'                                     | '.world'
        1    | 'com.logback.Foobar'                         | 'c.l.Foobar'
        1    | 'c.logback.Foobar'                           | 'c.l.Foobar'
        1    | '..Foobar'                                   | '..Foobar'
        1    | 'com.logback.xyz.Foobar'                     | 'c.l.x.Foobar'

        13   | 'com.logback.xyz.Foobar'                     | 'c.l.x.Foobar'
        14   | 'com.logback.xyz.Foobar'                     | 'c.l.xyz.Foobar'
        15   | 'com.logback.alligator.Foobar'               | 'c.l.a.Foobar'
        21   | 'com.logback.wombat.alligator.Foobar'        | 'c.l.w.a.Foobar'
        22   | 'com.logback.wombat.alligator.Foobar'        | 'c.l.w.alligator.Foobar'
        1    | 'com.logback.wombat.alligator.tomato.Foobar' | 'c.l.w.a.t.Foobar'
        21   | 'com.logback.wombat.alligator.tomato.Foobar' | 'c.l.w.a.tomato.Foobar'
        29   | 'com.logback.wombat.alligator.tomato.Foobar' | 'c.l.w.alligator.tomato.Foobar'
    }
}
