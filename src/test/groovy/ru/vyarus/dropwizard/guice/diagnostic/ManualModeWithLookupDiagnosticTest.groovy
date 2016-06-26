package ru.vyarus.dropwizard.guice.diagnostic

import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.diagnostic.support.ManualAppWithLookup
import ru.vyarus.dropwizard.guice.diagnostic.support.features.FooInstaller
import ru.vyarus.dropwizard.guice.diagnostic.support.features.FooModule
import ru.vyarus.dropwizard.guice.diagnostic.support.features.FooResource
import ru.vyarus.dropwizard.guice.module.GuiceSupportModule
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.JerseyFeatureInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.ResourceInstaller
import ru.vyarus.dropwizard.guice.module.jersey.debug.HK2DebugBundle
import ru.vyarus.dropwizard.guice.module.jersey.debug.service.HK2DebugFeature
import ru.vyarus.dropwizard.guice.support.util.GuiceRestrictedConfigBundle
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 26.06.2016
 */
@UseGuiceyApp(ManualAppWithLookup)
class ManualModeWithLookupDiagnosticTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info

    def "Check diagnostic info correctness for enabled lookup"() {

        expect: "correct bundles info"
        // assigned in abstract test
        info.bundles as Set == [HK2DebugBundle, GuiceRestrictedConfigBundle] as Set
        info.bundlesFromLookup as Set == [HK2DebugBundle, GuiceRestrictedConfigBundle] as Set

        and: "correct installers info"
        // feature installer was installed transitively by Hk2DebugBundle
        info.installers as Set == [ResourceInstaller, JerseyFeatureInstaller] as Set
        info.installersDisabled == [FooInstaller]
        info.installersFromScan.isEmpty()

        and: "correct extensions info"
        info.extensions as Set == [FooResource, HK2DebugFeature] as Set
        info.extensionsFromScan.isEmpty()
        info.getExtensions(ResourceInstaller) == [FooResource]
        info.getExtensions(JerseyFeatureInstaller) == [HK2DebugFeature]

        and: "correct modules"
        info.modules as Set == [FooModule, GuiceSupportModule, HK2DebugBundle.HK2DebugModule, GuiceRestrictedConfigBundle.GRestrictModule] as Set

    }
}
