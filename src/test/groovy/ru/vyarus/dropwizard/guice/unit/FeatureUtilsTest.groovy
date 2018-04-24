package ru.vyarus.dropwizard.guice.unit

import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils
import spock.lang.Specification


/**
 * @author Vyacheslav Rusakov
 * @since 24.04.2018
 */
class FeatureUtilsTest extends Specification {

    def "Check method not found"() {

        when: "find not allowed method"
        FeatureUtils.findMethod(FeatureUtils, "<init>")
        then: "err"
        def ex = thrown(IllegalStateException)
        ex.message.startsWith("Failed to obtain method")
    }

    def "Check method invocation error"() {

        when: "calling not allowed method"
        FeatureUtils.invokeMethod(FeatureUtils.findMethod(Clz, "call"), null)
        then: "err"
        def ex = thrown(IllegalStateException)
        ex.message.startsWith("Failed to invoke method")
    }

    static class Clz {
        public void call() {

        }
    }
}