package ru.vyarus.dropwizard.guice.bundles.lookup

import ru.vyarus.dropwizard.guice.bundle.lookup.VoidBundleLookup
import spock.lang.Specification


/**
 * @author Vyacheslav Rusakov
 * @since 17.01.2016
 */
class VoidLookupTest extends Specification {

    def "Check void lookup"() {

        expect: "empty list"
        new VoidBundleLookup().lookup() == []

    }
}