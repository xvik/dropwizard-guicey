package ru.vyarus.dropwizard.guice.config

import io.dropwizard.Application
import io.dropwizard.ConfiguredBundle
import ru.vyarus.dropwizard.guice.bundle.GuiceyBundleLookup
import ru.vyarus.dropwizard.guice.module.context.ConfigItem
import ru.vyarus.dropwizard.guice.module.context.ConfigScope
import ru.vyarus.dropwizard.guice.module.context.Filters
import ru.vyarus.dropwizard.guice.debug.ConfigurationDiagnostic
import ru.vyarus.dropwizard.guice.module.context.info.ItemId
import ru.vyarus.dropwizard.guice.module.context.info.ItemInfo
import ru.vyarus.dropwizard.guice.module.installer.feature.health.HealthCheckInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.JerseyFeatureInstaller
import ru.vyarus.dropwizard.guice.module.installer.scanner.ClasspathScanner
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
        !Filters.enabled().test(item(ConfigItem.Installer, JerseyFeatureInstaller) {
            disabledBy.add(Application)
        })

        and: "not supported items enabled"
        Filters.enabled().test(item(ConfigItem.Module, HK2DebugBundle.HK2DebugModule) {})

    }

    def "Check disabledBy filter"() {

        expect: "matched check"
        Filters.disabledBy(Application).test(item(ConfigItem.Installer, JerseyFeatureInstaller) {
            disabledBy.add(ItemId.from(Application))
        })

        and: "not matched check"
        !Filters.disabledBy(Application).test(item(ConfigItem.Installer, JerseyFeatureInstaller) {
            disabledBy.add(ItemId.from(ConfiguredBundle))
        })

    }

    def "Check scan filter"() {

        expect: "from scan"
        Filters.fromScan().test(item(ConfigItem.Installer, JerseyFeatureInstaller) {
            registeredBy.add(ItemId.from(ClasspathScanner))
        })

        and: "not from scan"
        !Filters.fromScan().test(item(ConfigItem.Installer, JerseyFeatureInstaller) {})

        and: "item not support scan"
        !Filters.fromScan().test(item(ConfigItem.Module, HK2DebugBundle.HK2DebugModule) {})

    }

    def "Check registrationScope filter"() {

        expect: "matched check"
        Filters.registrationScope(ConfigScope.Application).test(item(ConfigItem.Installer, JerseyFeatureInstaller) {
            countRegistrationAttempt(ItemId.from(Application))
        })

        and: "not matched check"
        !Filters.registrationScope(Application).test(item(ConfigItem.Installer, JerseyFeatureInstaller) {
            countRegistrationAttempt(ItemId.from(ConfiguredBundle))
        })

    }

    def "Check registeredBy filter"() {

        expect: "matched check"
        Filters.registeredBy(ConfigScope.Application).test(item(ConfigItem.Installer, JerseyFeatureInstaller) {
            registeredBy.add(ItemId.from(Application))
        })

        and: "not matched check"
        !Filters.registeredBy(Application).test(item(ConfigItem.Installer, JerseyFeatureInstaller) {
            registeredBy.add(ItemId.from(ConfiguredBundle))
        })

    }

    def "Check type filter"() {

        expect: "matched check"
        Filters.type(ConfigItem.Installer).test(item(ConfigItem.Installer, JerseyFeatureInstaller) {})
        Filters.type(JerseyFeatureInstaller).test(item(ConfigItem.Installer, JerseyFeatureInstaller) {})

        and: "not matched check"
        !Filters.type(ConfigItem.Bundle).test(item(ConfigItem.Installer, JerseyFeatureInstaller) {})
        !Filters.type(HealthCheckInstaller).test(item(ConfigItem.Installer, JerseyFeatureInstaller) {})

    }

    def "Check lookupBundles filter"() {

        expect: "matched check"
        Filters.lookupBundles().test(item(ConfigItem.Bundle, ConfigurationDiagnostic) {
            registeredBy.add(ItemId.from(GuiceyBundleLookup))
        })

        and: "not matched check"
        !Filters.lookupBundles().test(item(ConfigItem.Bundle, ConfigurationDiagnostic) {})

    }

    def "Check installedBy filter"() {

        expect: "matched check"
        Filters.installedBy(JerseyFeatureInstaller).test(item(ConfigItem.Extension, HK2DebugFeature) {
            installedBy = JerseyFeatureInstaller
        })

        and: "not matched check"
        !Filters.installedBy(JerseyFeatureInstaller).test(item(ConfigItem.Extension, HK2DebugFeature) {})

    }

    private static ItemInfo item(ConfigItem itemType, Class<?> type, Closure init) {
        ItemInfo info = itemType.newContainer(type)
        info.with init
        info
    }
}