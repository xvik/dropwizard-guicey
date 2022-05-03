package ru.vyarus.dropwizard.guice.diagnostic


import ru.vyarus.dropwizard.guice.bundle.lookup.PropertyBundleLookup
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook
import ru.vyarus.dropwizard.guice.module.context.SharedConfigurationState
import ru.vyarus.dropwizard.guice.module.jersey.debug.HK2DebugBundle
import ru.vyarus.dropwizard.guice.support.util.GuiceRestrictedConfigBundle
import ru.vyarus.dropwizard.guice.test.EnableHook
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 14.04.2018
 */
abstract class BaseDiagnosticTest extends Specification {

    // enable bundles lookup
    @EnableHook
    static GuiceyConfigurationHook LOOKUP_HOOK = {
        PropertyBundleLookup.enableBundles(HK2DebugBundle, GuiceRestrictedConfigBundle)
    }

    void setupSpec() {
        assert System.getProperty(PropertyBundleLookup.BUNDLES_PROPERTY)
    }

    void cleanupSpec() {
        // some tests are intentionally failing so be sure to remove stale applications
        SharedConfigurationState.clear()
        System.clearProperty(PropertyBundleLookup.BUNDLES_PROPERTY)
    }
}
