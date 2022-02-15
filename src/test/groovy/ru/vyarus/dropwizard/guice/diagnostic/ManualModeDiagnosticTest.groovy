package ru.vyarus.dropwizard.guice.diagnostic

import io.dropwizard.Application
import ru.vyarus.dropwizard.guice.diagnostic.support.ManualApp
import ru.vyarus.dropwizard.guice.diagnostic.support.features.FooInstaller
import ru.vyarus.dropwizard.guice.diagnostic.support.features.FooModule
import ru.vyarus.dropwizard.guice.diagnostic.support.features.FooResource
import ru.vyarus.dropwizard.guice.module.GuiceBootstrapModule
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.context.ConfigScope
import ru.vyarus.dropwizard.guice.module.context.info.InstallerItemInfo
import ru.vyarus.dropwizard.guice.module.context.info.ItemId
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.ResourceInstaller
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp

import javax.inject.Inject

import static ru.vyarus.dropwizard.guice.module.context.info.ItemId.typesOnly

/**
 * @author Vyacheslav Rusakov
 * @since 26.06.2016
 */
@TestGuiceyApp(ManualApp)
class ManualModeDiagnosticTest extends BaseDiagnosticTest {

    @Inject
    GuiceyConfigurationInfo info

    def "Check diagnostic info correctness"() {

        expect: "correct bundles info"
        info.guiceyBundles.isEmpty()
        info.bundlesFromLookup.isEmpty()

        and: "correct installers info"
        info.installers == [ResourceInstaller]
        info.installersDisabled == [FooInstaller]
        info.installersFromScan.isEmpty()

        and: "correct extensions info"
        info.extensions == [FooResource]
        info.extensionsFromScan.isEmpty()
        info.getExtensions(ResourceInstaller) == [FooResource]

        and: "correct modules"
        info.modules as Set == [FooModule, GuiceBootstrapModule] as Set

        and: "correct scopes"
        typesOnly(info.getActiveScopes()) as Set == [Application] as Set
        typesOnly(info.getItemsByScope(ConfigScope.Application)) as Set == [FooModule, ResourceInstaller, FooResource, GuiceBootstrapModule] as Set

        and: "foo installer was disabled"
        !info.getItemsByScope(ConfigScope.Application).contains(ItemId.from(FooInstaller))
        InstallerItemInfo fi = info.getInfo(FooInstaller)
        !fi.enabled
        fi.disabledBy == [ItemId.from(Application)] as Set
        fi.registered
        fi.registeredDirectly
    }
}
