package ru.vyarus.dropwizard.guice.diagnostic

import io.dropwizard.core.Application
import ru.vyarus.dropwizard.guice.diagnostic.support.ManualAppWithBundle
import ru.vyarus.dropwizard.guice.diagnostic.support.bundle.FooBundle
import ru.vyarus.dropwizard.guice.diagnostic.support.bundle.FooBundleResource
import ru.vyarus.dropwizard.guice.diagnostic.support.features.FooInstaller
import ru.vyarus.dropwizard.guice.diagnostic.support.features.FooModule
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.context.ConfigItem
import ru.vyarus.dropwizard.guice.module.context.ConfigScope
import ru.vyarus.dropwizard.guice.module.context.info.*
import ru.vyarus.dropwizard.guice.module.installer.feature.ManagedInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.ResourceInstaller
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import spock.lang.Specification

import jakarta.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 11.07.2016
 */
@TestGuiceyApp(ManualAppWithBundle)
class ConfigInfoItemsTest extends Specification {

    @Inject
    GuiceyConfigurationInfo info

    def "Check core objects accessible"() {

        expect:
        info.getData() != null
        info.getOptions() != null
        info.getStats() != null
        info.getConfigurationTree() != null
    }

    def "Check info objects correctness"() {

        expect: "module info"
        ItemInfo mi = info.getInfo(FooModule)
        mi.registeredDirectly
        mi.registered
        mi.registrationAttempts == 1
        mi.itemType == ConfigItem.Module
        mi.type == FooModule
        mi.registeredBy == [ItemId.from(Application)] as Set
        mi.registrationScope == ItemId.from(Application)
        mi.registrationScopeType == ConfigScope.Application
        mi.toString() ==~ /$ConfigItem.Module $FooModule.simpleName@[\w]+ \(#1\)/

        and: "bundle info"
        GuiceyBundleItemInfo bi = info.getInfo(FooBundle)
        bi.registeredDirectly
        bi.registered
        bi.registrationAttempts == 1
        bi.itemType == ConfigItem.Bundle
        bi.type == FooBundle
        bi.registeredBy == [ItemId.from(Application)] as Set
        bi.registrationScope == ItemId.from(Application)
        !bi.fromLookup
        bi.toString() ==~ /$ConfigItem.Bundle $FooBundle.simpleName@[\w]+ \(#1\)/

        and: "installer info"
        InstallerItemInfo ii = info.getInfo(ResourceInstaller)
        ii.registeredDirectly
        ii.registered
        ii.registrationAttempts == 1
        ii.itemType == ConfigItem.Installer
        ii.type == ResourceInstaller
        ii.registeredBy == [ItemId.from(Application)] as Set
        ii.registrationScope == ItemId.from(Application)
        !ii.fromScan
        ii.enabled
        ii.disabledBy.isEmpty()
        ii.toString() == "$ConfigItem.Installer $ResourceInstaller.simpleName" as String

        and: "extensions info"
        ExtensionItemInfo ei = info.getInfo(FooBundleResource)
        !ei.registeredDirectly
        ei.registered
        ei.registrationAttempts == 1
        ei.itemType == ConfigItem.Extension
        ei.type == FooBundleResource
        ei.registeredBy == [ItemId.from(FooBundle)] as Set
        ei.registrationScope.getType() == FooBundle
        !ei.fromScan
        !ei.jerseyManaged
        !ei.lazy
        ei.installedBy == ResourceInstaller
        ei.toString() == "$ConfigItem.Extension $FooBundleResource.simpleName" as String

    }

    def "Check disabled installer cases"() {

        expect: "disabled installer info"
        InstallerItemInfo dii = info.getInfo(FooInstaller)
        dii.registeredDirectly
        dii.registered
        dii.registrationAttempts == 1
        dii.itemType == ConfigItem.Installer
        dii.type == FooInstaller
        dii.registeredBy == [ItemId.from(Application)] as Set
        dii.registrationScope == ItemId.from(Application)
        !dii.fromScan
        !dii.enabled
        dii.disabledBy == [ItemId.from(Application)] as Set
        dii.toString() == "$ConfigItem.Installer $FooInstaller.simpleName" as String

        and: "disabled never registered installer info"
        InstallerItemInfo dnr = info.getInfo(ManagedInstaller)
        !dnr.registeredDirectly
        !dnr.registered
        dnr.registrationAttempts == 0
        dnr.itemType == ConfigItem.Installer
        dnr.type == ManagedInstaller
        dnr.registeredBy.isEmpty()
        !dnr.fromScan
        !dnr.enabled
        dnr.disabledBy == [ItemId.from(FooBundle)] as Set
        dnr.toString() == "$ConfigItem.Installer $ManagedInstaller.simpleName" as String

    }
}