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

    final String SERVICE_FILE = "META-INF/services/${GuiceyBundle.class.name}"
    String servicesPath

    void setup() {
        servicesPath = getClass().getResource("/").toURI().getRawPath() + SERVICE_FILE
        new File(servicesPath).parentFile.mkdirs()
    }

    void cleanup() {
        File file = new File(servicesPath)
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
        new File(servicesPath) << HK2DebugBundle.class.name
        def res = new ServiceLoaderBundleLookup().lookup()
        then: "loaded"
        res.size() == 1
        res[0] instanceof HK2DebugBundle
    }
}