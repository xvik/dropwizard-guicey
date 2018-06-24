package ru.vyarus.dropwizard.guice.diagnostic

import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.bundle.lookup.PropertyBundleLookup
import ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup
import ru.vyarus.dropwizard.guice.module.jersey.debug.HK2DebugBundle
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook
import ru.vyarus.dropwizard.guice.support.util.GuiceRestrictedConfigBundle
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyConfiguration
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 14.04.2018
 */
@UseGuiceyConfiguration(LookupHook)
abstract class BaseDiagnosticTest extends Specification {

    void setupSpec() {
        assert System.getProperty(PropertyBundleLookup.BUNDLES_PROPERTY)
    }

    void cleanupSpec() {
        // some tests are intentionally failing so be sure to remove stale applications
        InjectorLookup.clear()
    }

    static class LookupHook implements GuiceyConfigurationHook {
        @Override
        void configure(GuiceBundle.Builder builder) {
            // enable bundles lookup
            PropertyBundleLookup.enableBundles(HK2DebugBundle, GuiceRestrictedConfigBundle)
        }
    }
}
