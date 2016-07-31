package ru.vyarus.dropwizard.guice.config

import io.dropwizard.Application
import io.dropwizard.Bundle
import ru.vyarus.dropwizard.guice.bundle.GuiceyBundleLookup
import ru.vyarus.dropwizard.guice.module.context.ConfigItem
import ru.vyarus.dropwizard.guice.module.context.Filters
import ru.vyarus.dropwizard.guice.module.context.debug.DiagnosticBundle
import ru.vyarus.dropwizard.guice.module.context.info.ItemInfo
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
        !Filters.enabled().apply(item(ConfigItem.Installer, JerseyFeatureInstaller) {
            disabledBy.add(Application)
        })

        and: "not supported items enabled"
        Filters.enabled().apply(item(ConfigItem.Module, HK2DebugBundle.HK2DebugModule) {})

    }

    def "Check disabledBy filter"() {

        expect: "matched check"
        Filters.disabledBy(Application).apply(item(ConfigItem.Installer, JerseyFeatureInstaller) {
            disabledBy.add(Application)
        })

        and: "not matched check"
        !Filters.disabledBy(Application).apply(item(ConfigItem.Installer, JerseyFeatureInstaller) {
            disabledBy.add(Bundle)
        })

    }

    def "Check scan filter"() {

        expect: "from scan"
        Filters.fromScan().apply(item(ConfigItem.Installer, JerseyFeatureInstaller) {
            registeredBy.add(ClasspathScanner)
        })

        and: "not from scan"
        !Filters.fromScan().apply(item(ConfigItem.Installer, JerseyFeatureInstaller) {})

        and: "item not support scan"
        !Filters.fromScan().apply(item(ConfigItem.Module, HK2DebugBundle.HK2DebugModule) {})

    }

    def "Check registrationScope filter"() {

        expect: "matched check"
        Filters.registrationScope(Application).apply(item(ConfigItem.Installer, JerseyFeatureInstaller) {
            registrationScope = Application
        })

        and: "not matched check"
        !Filters.registrationScope(Application).apply(item(ConfigItem.Installer, JerseyFeatureInstaller) {
            registrationScope = Bundle
        })

    }

    def "Check registeredBy filter"() {

        expect: "matched check"
        Filters.registeredBy(Application).apply(item(ConfigItem.Installer, JerseyFeatureInstaller) {
            registeredBy.add(Application)
        })

        and: "not matched check"
        !Filters.registeredBy(Application).apply(item(ConfigItem.Installer, JerseyFeatureInstaller) {
            registeredBy.add(Bundle)
        })

    }

    def "Check type filter"() {

        expect: "matched check"
        Filters.type(ConfigItem.Installer).apply(item(ConfigItem.Installer, JerseyFeatureInstaller) {})

        and: "not matched check"
        !Filters.type(ConfigItem.Bundle).apply(item(ConfigItem.Installer, JerseyFeatureInstaller) {})

    }

    def "Check lookupBundles filter"() {

        expect: "matched check"
        Filters.lookupBundles().apply(item(ConfigItem.Bundle, DiagnosticBundle) {
            registeredBy.add(GuiceyBundleLookup)
        })

        and: "not matched check"
        !Filters.lookupBundles().apply(item(ConfigItem.Bundle, DiagnosticBundle) {})

    }

    def "Check dwBundles filter"() {

        expect: "matched check"
        Filters.dwBundles().apply(item(ConfigItem.Bundle, DiagnosticBundle) {
            registeredBy.add(Bundle)
        })

        and: "not matched check"
        !Filters.dwBundles().apply(item(ConfigItem.Bundle, DiagnosticBundle) {})

    }

    def "Check installedBy filter"() {

        expect: "matched check"
        Filters.installedBy(JerseyFeatureInstaller).apply(item(ConfigItem.Extension, HK2DebugFeature) {
            installedBy = JerseyFeatureInstaller
        })

        and: "not matched check"
        !Filters.installedBy(JerseyFeatureInstaller).apply(item(ConfigItem.Extension, HK2DebugFeature) {})

    }

    private static ItemInfo item(ConfigItem itemType, Class<?> type, Closure init) {
        ItemInfo info = itemType.newContainer(type)
        info.with init
        info
    }
}