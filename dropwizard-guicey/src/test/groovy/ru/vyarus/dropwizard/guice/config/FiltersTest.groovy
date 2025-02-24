package ru.vyarus.dropwizard.guice.config

import io.dropwizard.core.Application
import io.dropwizard.core.ConfiguredBundle
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.bundle.GuiceyBundleLookup
import ru.vyarus.dropwizard.guice.debug.ConfigurationDiagnostic
import ru.vyarus.dropwizard.guice.module.context.ConfigItem
import ru.vyarus.dropwizard.guice.module.context.ConfigScope
import ru.vyarus.dropwizard.guice.module.context.Filters
import ru.vyarus.dropwizard.guice.module.context.info.ItemId
import ru.vyarus.dropwizard.guice.module.context.info.impl.*
import ru.vyarus.dropwizard.guice.module.installer.feature.ManagedInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.health.HealthCheckInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.JerseyFeatureInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.ResourceInstaller
import ru.vyarus.dropwizard.guice.module.installer.install.JerseyInstaller
import ru.vyarus.dropwizard.guice.module.installer.install.WebInstaller
import ru.vyarus.dropwizard.guice.module.installer.scanner.ClasspathScanner
import ru.vyarus.dropwizard.guice.module.jersey.Jersey2Module
import ru.vyarus.dropwizard.guice.module.jersey.debug.HK2DebugBundle
import ru.vyarus.dropwizard.guice.module.jersey.debug.service.HK2DebugFeature
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 26.07.2016
 */
class FiltersTest extends Specification {

    def "Check enabled filter"() {

        expect: "disabled item filtered"
        !Filters.enabled().test(installer(JerseyFeatureInstaller) {
            disabledBy.add(ItemId.from(Application))
        })

        and: "enabled item filtered"
        Filters.disabled().test(installer(JerseyFeatureInstaller) {
            disabledBy.add(ItemId.from(Application))
        })

        and: "not supported items enabled"
        Filters.enabled().test(module(HK2DebugBundle.HK2DebugModule) {})

    }

    def "Check disabledBy filter"() {

        expect: "matched check"
        Filters.disabledBy(Application).test(installer(JerseyFeatureInstaller) {
            disabledBy.add(ItemId.from(Application))
        })

        and: "not matched check"
        !Filters.disabledBy(Application).test(installer(JerseyFeatureInstaller) {
            disabledBy.add(ItemId.from(ConfiguredBundle))
        })

    }

    def "Check scan filter"() {

        expect: "from scan"
        Filters.fromScan().test(installer(JerseyFeatureInstaller) {
            registeredBy.add(ItemId.from(ClasspathScanner))
        })

        and: "not from scan"
        !Filters.fromScan().test(installer(JerseyFeatureInstaller))

        and: "item not support scan"
        !Filters.fromScan().test(module(HK2DebugBundle.HK2DebugModule))

    }

    def "Check registrationScope filter"() {

        expect: "matched check"
        Filters.registrationScope(ConfigScope.Application).test(installer(JerseyFeatureInstaller) {
            countRegistrationAttempt(ItemId.from(Application))
        })

        and: "not matched check"
        !Filters.registrationScope(Application).test(installer(JerseyFeatureInstaller) {
            countRegistrationAttempt(ItemId.from(ConfiguredBundle))
        })

    }

    def "Check registeredBy filter"() {

        expect: "matched check"
        Filters.registeredBy(ConfigScope.Application).test(installer(JerseyFeatureInstaller) {
            registeredBy.add(ItemId.from(Application))
        })

        and: "not matched check"
        !Filters.registeredBy(Application).test(installer(JerseyFeatureInstaller) {
            registeredBy.add(ItemId.from(ConfiguredBundle))
        })

    }

    def "Check type filter"() {

        expect: "matched check"
        Filters.type(ConfigItem.Installer).test(installer(JerseyFeatureInstaller))
        Filters.type(JerseyFeatureInstaller).test(installer(JerseyFeatureInstaller))

        and: "not matched check"
        !Filters.type(ConfigItem.Bundle).test(installer(JerseyFeatureInstaller))
        !Filters.type(HealthCheckInstaller).test(installer(JerseyFeatureInstaller))
        !Filters.type(HealthCheckInstaller).test(installer(JerseyFeatureInstaller))

    }

    def "Check type filter shortcuts"() {

        expect: "bundles matched"
        Filters.bundles().test(bundle(ConfigurationDiagnostic))
        Filters.bundles().test(dropwizardBundle(GuiceBundle))
        !Filters.bundles().test(installer(JerseyFeatureInstaller))

        and: "guicey bundles matched"
        Filters.guiceyBundles().test(bundle(ConfigurationDiagnostic))
        !Filters.guiceyBundles().test(dropwizardBundle(GuiceBundle))

        and: "guicey dropwizard bundles matched"
        !Filters.dropwizardBundles().test(bundle(ConfigurationDiagnostic))
        Filters.dropwizardBundles().test(dropwizardBundle(GuiceBundle))

        and: "installers matched"
        Filters.installers().test(installer(ResourceInstaller))
        !Filters.installers().test(bundle(ConfigurationDiagnostic))

        and: "modules matched"
        Filters.modules().test(module(Jersey2Module))
        !Filters.modules().test(bundle(ConfigurationDiagnostic))
    }

    def "Check web and jersey extensions"() {

        expect: 
        Filters.extensions().and(Filters.webExtension()).test(extension(HK2DebugFeature){
            installedBy = WebInstaller
        })
        Filters.extensions().and(Filters.jerseyExtension()).test(extension(HK2DebugFeature) {
            installedBy = JerseyInstaller
        })
        !Filters.extensions().and(Filters.jerseyExtension()).test(extension(HK2DebugFeature) {
            installedBy = ManagedInstaller
        })
    }

    def "Check lookupBundles filter"() {

        expect: "matched check"
        Filters.lookupBundles().test(bundle(ConfigurationDiagnostic) {
            registeredBy.add(ItemId.from(GuiceyBundleLookup))
        })

        and: "not matched check"
        !Filters.lookupBundles().test(bundle(ConfigurationDiagnostic))

    }

    def "Check installedBy filter"() {

        expect: "matched check"
        Filters.installedBy(JerseyFeatureInstaller).test(extension(HK2DebugFeature) {
            installedBy = JerseyFeatureInstaller
        })

        and: "not matched check"
        !Filters.installedBy(JerseyFeatureInstaller).test(extension(HK2DebugFeature))

    }

    private static ModuleItemInfoImpl module(Class ext, @DelegatesTo(ModuleItemInfoImpl) Closure config = null) {
        return item(ConfigItem.Module, ext, config)
    }

    private static InstallerItemInfoImpl installer(Class ext, @DelegatesTo(InstallerItemInfoImpl) Closure config = null) {
        return item(ConfigItem.Installer, ext, config)
    }

    private static GuiceyBundleItemInfoImpl bundle(Class ext, @DelegatesTo(GuiceyBundleItemInfoImpl) Closure config = null) {
        return item(ConfigItem.Bundle, ext, config)
    }

    private static DropwizardBundleItemInfoImpl dropwizardBundle(Class ext, @DelegatesTo(DropwizardBundleItemInfoImpl) Closure config = null) {
        return item(ConfigItem.DropwizardBundle, ext, config)
    }

    private static ExtensionItemInfoImpl extension(Class ext, @DelegatesTo(ExtensionItemInfoImpl) Closure config = null) {
        return item(ConfigItem.Extension, ext, config)
    }

    private static <T extends ItemInfoImpl> T item(ConfigItem itemType, Class type, Closure config = null) {
        T res = itemType.<T>newContainer(type)
        if (config) {
            res.with config
        }
        return res;
    }
}