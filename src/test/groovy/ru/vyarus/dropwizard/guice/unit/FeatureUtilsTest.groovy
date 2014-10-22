package ru.vyarus.dropwizard.guice.unit

import ru.vyarus.dropwizard.guice.module.installer.util.GenericsUtils
import ru.vyarus.dropwizard.guice.support.util.Base
import ru.vyarus.dropwizard.guice.support.util.GenerifiedInterface
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov 
 * @since 22.10.2014
 */
class FeatureUtilsTest extends Specification {

    def "Check generics resolution"() {

        when: "resolving all types of interface generics"
        String[] res = GenericsUtils.getInterfaceGenericsAsStrings(Base, GenerifiedInterface)
        then: "everything is ok"
        res == ['Integer', 'String[]', 'List<String>', 'List<Set<String>>']
    }
}