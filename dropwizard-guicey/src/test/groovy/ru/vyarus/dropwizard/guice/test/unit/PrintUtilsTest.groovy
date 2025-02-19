package ru.vyarus.dropwizard.guice.test.unit

import ru.vyarus.dropwizard.guice.test.util.PrintUtils
import spock.lang.Specification

import java.util.concurrent.TimeUnit

/**
 * @author Vyacheslav Rusakov
 * @since 17.02.2025
 */
class PrintUtilsTest extends Specification {

    def "Check toStringValue"() {

        expect:
        PrintUtils.toStringValue(value, 10) == result

        where:
        value                                   | result
        "longlonglonglong"                      | "longlonglo..."
        1                                       | "1"
        1.2                                     | "1.2"
        Integer.valueOf(12)                     | "12"
        Double.valueOf("12.1")                  | "12.1"
        BigDecimal.valueOf(12.1)                | "12.1"
        Boolean.TRUE                            | "true"
        false                                   | "false"
        null                                    | "null"
        ""                                      | ""
        [1, 2, 3]                               | "(3)[ 1,2,3 ]"
        []                                      | "(0)[]"
        [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12] | "(12)[ 1,2,3,4,5,6,7,8,9,10,... ]"
        [1, 2, 3] as Object[]                   | "(3)[ 1,2,3 ]"
        new Object()                            | "Object@" + PrintUtils.identity(value)
    }

    def "Check identity hash"() {

        when: "construct simple object"
        Object obj = new Object()

        then: "hash should be equal to default toString"
        obj.toString().contains('@' + PrintUtils.identity(obj))

    }

    def "Check metric format"() {

        expect:
        PrintUtils.formatMetric(value, unit) == result

        where:
        value | unit                  | result
        100   | null                  | "100.000"
        100   | TimeUnit.NANOSECONDS  | "100.000 ns"
        100   | TimeUnit.MICROSECONDS | "0.100 μs"
        100   | TimeUnit.MILLISECONDS | "0.000 ms"
        100   | TimeUnit.SECONDS      | "0.000 s"
        100   | TimeUnit.MINUTES      | "0.000 m"
        100   | TimeUnit.HOURS        | "0.000 h"
        100   | TimeUnit.DAYS         | "0.000 d"
    }
}
