package ru.vyarus.dropwizard.guice.diagnostic

import io.dropwizard.Application
import ru.vyarus.dropwizard.guice.diagnostic.support.ManualAppWithBundle
import ru.vyarus.dropwizard.guice.diagnostic.support.bundle.*
import ru.vyarus.dropwizard.guice.diagnostic.support.features.FooInstaller
import ru.vyarus.dropwizard.guice.diagnostic.support.features.FooModule
import ru.vyarus.dropwizard.guice.diagnostic.support.features.FooResource
import ru.vyarus.dropwizard.guice.module.GuiceSupportModule
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.context.info.InstallerItemInfo
import ru.vyarus.dropwizard.guice.module.installer.CoreInstallersBundle
import ru.vyarus.dropwizard.guice.module.installer.feature.ManagedInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.ResourceInstaller
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 26.06.2016
 */
@UseGuiceyApp(ManualAppWithBundle)
class ManualModeWithBundleDiagnosticTest extends BaseDiagnosticTest {

    @Inject
    GuiceyConfigurationInfo info

    def "Check diagnostic info correctness"() {

        expect: "correct bundles info"
        info.bundles as Set == [FooBundle, FooBundleRelativeBundle] as Set
        info.bundlesFromLookup.isEmpty()
        info.bundlesFromDw.isEmpty()

        and: "correct installers info"
        info.installers as Set == [ResourceInstaller, FooBundleInstaller] as Set
        info.installersDisabled as Set == [FooInstaller, ManagedInstaller] as Set
        info.installersFromScan.isEmpty()

        and: "correct extensions info"
        info.extensions as Set == [FooResource, FooBundleResource] as Set
        info.extensionsFromScan.isEmpty()
        info.getExtensions(ResourceInstaller) as Set == [FooResource, FooBundleResource] as Set
        info.getExtensions(FooBundleInstaller).isEmpty()

        and: "correct modules"
        info.modules as Set == [FooModule, GuiceSupportModule, FooBundleModule] as Set

        and: "correct scopes"
        info.getActiveScopes() == [Application, FooBundle] as Set
        info.getItemsByScope(Application) as Set == [FooModule, ResourceInstaller, FooResource, FooBundle, GuiceSupportModule] as Set
        info.getItemsByScope(FooBundle) as Set == [FooBundleInstaller, FooBundleResource, FooBundleModule, FooBundleRelativeBundle] as Set

        and: "foo installer was disabled"
        !info.getItemsByScope(Application).contains(FooInstaller)
        InstallerItemInfo fi = info.data.getInfo(FooInstaller)
        !fi.enabled
        fi.disabledBy == [Application] as Set
        fi.registered
        fi.registeredDirectly

        and: "managed installer was disabled and never registered"
        !info.getItemsByScope(CoreInstallersBundle).contains(ManagedInstaller)
        InstallerItemInfo mi = info.data.getInfo(ManagedInstaller)
        !mi.enabled
        mi.disabledBy == [FooBundle] as Set
        !mi.registered
        mi.registrationAttempts == 0
        !mi.registeredDirectly
    }
}
