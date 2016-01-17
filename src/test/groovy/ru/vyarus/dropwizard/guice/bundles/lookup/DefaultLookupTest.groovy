package ru.vyarus.dropwizard.guice.bundles.lookup

import ru.vyarus.dropwizard.guice.bundle.DefaultBundleLookup
import ru.vyarus.dropwizard.guice.bundle.lookup.PropertyBundleLookup
import ru.vyarus.dropwizard.guice.module.jersey.debug.HK2DebugBundle
import spock.lang.Specification


/**
 * @author Vyacheslav Rusakov
 * @since 17.01.2016
 */
class DefaultLookupTest extends Specification {

    def "Check default lookup"() {

        when: "nothing registered"
        System.clearProperty(PropertyBundleLookup.BUNDLES_PROPERTY)
        then: "nothing found"
        new DefaultBundleLookup().lookup() == []

        when: "init property"
        PropertyBundleLookup.enableBundles(HK2DebugBundle)
        def res = new DefaultBundleLookup().lookup()
        then: "resolved"
        res.size() == 1
        res[0] instanceof HK2DebugBundle
    }
}