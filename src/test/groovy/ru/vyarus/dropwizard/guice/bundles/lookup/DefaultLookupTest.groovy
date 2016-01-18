package ru.vyarus.dropwizard.guice.bundles.lookup

import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.bundle.DefaultBundleLookup
import ru.vyarus.dropwizard.guice.bundle.lookup.PropertyBundleLookup
import ru.vyarus.dropwizard.guice.module.installer.CoreInstallersBundle
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
import ru.vyarus.dropwizard.guice.module.jersey.debug.HK2DebugBundle

/**
 * @author Vyacheslav Rusakov
 * @since 17.01.2016
 */
class DefaultLookupTest extends AbstractTest {

    String serviceFile = "build/classes/test/META-INF/services/${GuiceyBundle.class.name}"

    void setup() {
        new File(serviceFile).parentFile.mkdirs()
    }

    void cleanup() {
        File file = new File(serviceFile)
        if (file.exists()) {
            file.delete()
        }
    }

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

        when: "init from both loaders"
        PropertyBundleLookup.enableBundles(HK2DebugBundle)
        new File(serviceFile) << CoreInstallersBundle.class.name
        res = new DefaultBundleLookup().lookup()
        then: "resolved"
        res.size() == 2
        res[0] instanceof HK2DebugBundle
        res[1] instanceof CoreInstallersBundle
    }
}