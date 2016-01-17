package ru.vyarus.dropwizard.guice.bundles.lookup

import ru.vyarus.dropwizard.guice.bundle.lookup.PropertyBundleLookup
import ru.vyarus.dropwizard.guice.module.installer.CoreInstallersBundle
import ru.vyarus.dropwizard.guice.module.jersey.debug.HK2DebugBundle
import spock.lang.Specification


/**
 * @author Vyacheslav Rusakov
 * @since 17.01.2016
 */
class PropertyLookupTest extends Specification {

    def "Check property not set"() {

        setup:
        System.clearProperty(PropertyBundleLookup.BUNDLES_PROPERTY)

        expect: "empty list"
        new PropertyBundleLookup().lookup() == []

    }

    def "Check property parsing"() {

        when: "single bundle set"
        System.setProperty(PropertyBundleLookup.BUNDLES_PROPERTY, HK2DebugBundle.class.name)
        def res = new PropertyBundleLookup().lookup()
        then: "parsed"
        res.size() == 1
        res[0] instanceof HK2DebugBundle


        when: "multiple bundle set"
        System.setProperty(PropertyBundleLookup.BUNDLES_PROPERTY,
                [HK2DebugBundle.class.name, CoreInstallersBundle.class.name].join(','))
        res = new PropertyBundleLookup().lookup()
        then: "parsed"
        res.size() == 2
        res[0] instanceof HK2DebugBundle
        res[1] instanceof CoreInstallersBundle


        when: "multiple bundle with spaces set"
        System.setProperty(PropertyBundleLookup.BUNDLES_PROPERTY,
                [HK2DebugBundle.class.name, CoreInstallersBundle.class.name].join('   ,  '))
        res = new PropertyBundleLookup().lookup()
        then: "parsed"
        res.size() == 2
        res[0] instanceof HK2DebugBundle
        res[1] instanceof CoreInstallersBundle
    }

    def "Check static shortcut"() {

        when: "single bundle set"
        PropertyBundleLookup.enableBundles(HK2DebugBundle.class)
        def res = new PropertyBundleLookup().lookup()
        then: "parsed"
        res.size() == 1
        res[0] instanceof HK2DebugBundle


        when: "multiple bundle set"
        PropertyBundleLookup.enableBundles(HK2DebugBundle, CoreInstallersBundle)
        res = new PropertyBundleLookup().lookup()
        then: "parsed"
        res.size() == 2
        res[0] instanceof HK2DebugBundle
        res[1] instanceof CoreInstallersBundle

    }

    def "Check parse fail"() {

        when: "bad value in property"
        System.setProperty(PropertyBundleLookup.BUNDLES_PROPERTY, "fdsfsdfsd")
        new PropertyBundleLookup().lookup()
        then: "failed"
        thrown(IllegalStateException)

    }
}