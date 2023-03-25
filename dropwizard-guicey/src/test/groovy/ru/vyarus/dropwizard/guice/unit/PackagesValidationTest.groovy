package ru.vyarus.dropwizard.guice.unit

import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.module.installer.scanner.ClasspathScanner

/**
 * @author Vyacheslav Rusakov 
 * @since 01.10.2015
 */
class PackagesValidationTest extends AbstractTest {

    def "Check packages integrity validation"() {

        when: "duplicate subpackage"
        new ClasspathScanner(['com.pack', 'com.pack.sub'] as Set)
        then: "not allowed"
        thrown(IllegalStateException)

        when: "duplicate subpackage inverse order"
        new ClasspathScanner(['com.pack.sub', 'com.pack'] as Set)
        then: "not allowed"
        thrown(IllegalStateException)

        when: "valid packages"
        new ClasspathScanner(['com.pack.sub', 'com.pack.sub2', 'com.pack.sub3.foo'] as Set)
        then: "valid"
        true

        when: "duplicate subpackage"
        new ClasspathScanner(['com.pack.sub', 'com.pack.sub2', 'com.pack.sub3.foo', 'com.pack.sub.bar'] as Set)
        then: "not allowed"
        thrown(IllegalStateException)
    }
}