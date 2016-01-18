package ru.vyarus.dropwizard.guice.bundles.lookup

import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.bundle.lookup.ServiceLoaderBundleLookup
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
import ru.vyarus.dropwizard.guice.module.jersey.debug.HK2DebugBundle

/**
 * @author Vyacheslav Rusakov
 * @since 18.01.2016
 */
class ServiceLoaderLookupTest extends AbstractTest {

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

    def "Check no services"() {

        expect: "empty list"
        new ServiceLoaderBundleLookup().lookup() == []
    }

    def "Check service load"() {

        when: "services defined"
        new File(serviceFile) << HK2DebugBundle.class.name
        def res = new ServiceLoaderBundleLookup().lookup()
        then: "loaded"
        res.size() == 1
        res[0] instanceof HK2DebugBundle
    }
}