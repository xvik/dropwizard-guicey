package ru.vyarus.dropwizard.guice.diagnostic

import io.dropwizard.Application
import ru.vyarus.dropwizard.guice.diagnostic.support.ManualAppWithBundle
import ru.vyarus.dropwizard.guice.diagnostic.support.bundle.FooBundle
import ru.vyarus.dropwizard.guice.diagnostic.support.bundle.FooBundleResource
import ru.vyarus.dropwizard.guice.diagnostic.support.features.FooInstaller
import ru.vyarus.dropwizard.guice.diagnostic.support.features.FooModule
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.context.ConfigItem
import ru.vyarus.dropwizard.guice.module.context.ConfigScope
import ru.vyarus.dropwizard.guice.module.context.info.BundleItemInfo
import ru.vyarus.dropwizard.guice.module.context.info.ExtensionItemInfo
import ru.vyarus.dropwizard.guice.module.context.info.InstallerItemInfo
import ru.vyarus.dropwizard.guice.module.context.info.ItemInfo
import ru.vyarus.dropwizard.guice.module.installer.feature.ManagedInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.ResourceInstaller
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp
import spock.lang.Specification

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 11.07.2016
 */
@UseGuiceyApp(ManualAppWithBundle)
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
        ItemInfo mi = info.data.getInfo(FooModule)
        mi.registeredDirectly
        mi.registered
        mi.registrationAttempts == 1
        mi.itemType == ConfigItem.Module
        mi.type == FooModule
        mi.registeredBy == [Application] as Set
        mi.registrationScope == Application
        mi.registrationScopeType == ConfigScope.Application
        mi.toString() == "$ConfigItem.Module $FooModule.simpleName" as String

        and: "bundle info"
        BundleItemInfo bi = info.data.getInfo(FooBundle)
        bi.registeredDirectly
        bi.registered
        bi.registrationAttempts == 1
        bi.itemType == ConfigItem.Bundle
        bi.type == FooBundle
        bi.registeredBy == [Application] as Set
        bi.registrationScope == Application
        !bi.fromLookup
        bi.toString() == "$ConfigItem.Bundle $FooBundle.simpleName" as String

        and: "installer info"
        InstallerItemInfo ii = info.data.getInfo(ResourceInstaller)
        ii.registeredDirectly
        ii.registered
        ii.registrationAttempts == 1
        ii.itemType == ConfigItem.Installer
        ii.type == ResourceInstaller
        ii.registeredBy == [Application] as Set
        ii.registrationScope == Application
        !ii.fromScan
        ii.enabled
        ii.disabledBy.isEmpty()
        ii.toString() == "$ConfigItem.Installer $ResourceInstaller.simpleName" as String

        and: "extensions info"
        ExtensionItemInfo ei = info.data.getInfo(FooBundleResource)
        !ei.registeredDirectly
        ei.registered
        ei.registrationAttempts == 1
        ei.itemType == ConfigItem.Extension
        ei.type == FooBundleResource
        ei.registeredBy == [FooBundle] as Set
        ei.registrationScope == FooBundle
        !ei.fromScan
        !ei.hk2Managed
        !ei.lazy
        ei.installedBy == ResourceInstaller
        ei.toString() == "$ConfigItem.Extension $FooBundleResource.simpleName" as String

    }

    def "Check disabled installer cases"() {

        expect: "disabled installer info"
        InstallerItemInfo dii = info.data.getInfo(FooInstaller)
        dii.registeredDirectly
        dii.registered
        dii.registrationAttempts == 1
        dii.itemType == ConfigItem.Installer
        dii.type == FooInstaller
        dii.registeredBy == [Application] as Set
        dii.registrationScope == Application
        !dii.fromScan
        !dii.enabled
        dii.disabledBy == [Application] as Set
        dii.toString() == "$ConfigItem.Installer $FooInstaller.simpleName" as String

        and: "disabled never registered installer info"
        InstallerItemInfo dnr = info.data.getInfo(ManagedInstaller)
        !dnr.registeredDirectly
        !dnr.registered
        dnr.registrationAttempts == 0
        dnr.itemType == ConfigItem.Installer
        dnr.type == ManagedInstaller
        dnr.registeredBy.isEmpty()
        !dnr.fromScan
        !dnr.enabled
        dnr.disabledBy == [FooBundle] as Set
        dnr.toString() == "$ConfigItem.Installer $ManagedInstaller.simpleName" as String

    }
}