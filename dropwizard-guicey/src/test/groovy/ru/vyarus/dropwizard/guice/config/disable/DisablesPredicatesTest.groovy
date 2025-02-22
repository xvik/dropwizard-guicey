package ru.vyarus.dropwizard.guice.config.disable

import com.google.inject.Binder
import com.google.inject.Module
import io.dropwizard.core.Application
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.module.context.ConfigItem
import ru.vyarus.dropwizard.guice.module.context.ConfigScope
import ru.vyarus.dropwizard.guice.module.context.Disables
import ru.vyarus.dropwizard.guice.module.context.info.DropwizardBundleItemInfo
import ru.vyarus.dropwizard.guice.module.context.info.ExtensionItemInfo
import ru.vyarus.dropwizard.guice.module.context.info.GuiceyBundleItemInfo
import ru.vyarus.dropwizard.guice.module.context.info.InstallerItemInfo
import ru.vyarus.dropwizard.guice.module.context.info.ItemId
import ru.vyarus.dropwizard.guice.module.context.info.ItemInfo
import ru.vyarus.dropwizard.guice.module.context.info.ModuleItemInfo
import ru.vyarus.dropwizard.guice.module.context.info.impl.DropwizardBundleItemInfoImpl
import ru.vyarus.dropwizard.guice.module.context.info.impl.ExtensionItemInfoImpl
import ru.vyarus.dropwizard.guice.module.context.info.impl.GuiceyBundleItemInfoImpl
import ru.vyarus.dropwizard.guice.module.context.info.impl.InstallerItemInfoImpl
import ru.vyarus.dropwizard.guice.module.context.info.impl.ItemInfoImpl
import ru.vyarus.dropwizard.guice.module.context.info.impl.ModuleItemInfoImpl
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.ManagedInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.health.HealthCheckInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.ResourceInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.web.WebFilterInstaller

import static ru.vyarus.dropwizard.guice.module.context.ConfigItem.*

/**
 * @author Vyacheslav Rusakov
 * @since 09.04.2018
 */
class DisablesPredicatesTest extends AbstractTest {

    def "Check predicates"() {

        expect:
        Disables.registeredBy(ConfigScope.Application).test(item(Extension, Sample))
        Disables.registeredBy(ConfigScope.Application.getKey()).test(item(Extension, Sample))
        !Disables.registeredBy(Serializable).test(item(Extension, Sample))
        !Disables.registeredBy(ItemId.from(Serializable)).test(item(Extension, Sample))

        Disables.itemType(Extension, Installer).test(item(Installer, Sample))
        !Disables.itemType(Extension, Installer).test(item(Bundle, Sample))

        Disables.extension().test(item(Extension, Sample))
        !Disables.extension().test(item(Installer, Sample))

        Disables.installer().test(item(Installer, Sample))
        !Disables.installer().test(item(Extension, Sample))

        Disables.module().test(item(ConfigItem.Module, Sample))
        !Disables.module().test(item(Extension, Sample))

        Disables.bundle().test(item(Bundle, Sample))
        !Disables.bundle().test(item(Extension, Sample))

        Disables.dropwizardBundle().test(item(DropwizardBundle, Sample))
        !Disables.dropwizardBundle().test(item(Bundle, Sample))

        Disables.type(Sample).test(item(Extension, Sample))
        !Disables.type(Sample).test(item(Extension, Sample2))

        Disables.inPackage('ru.vyarus').test(item(Extension, Sample))
        !Disables.inPackage('com.foo').test(item(Extension, Sample2))
    }

    def "Check extension predicates"() {

        expect:
        Disables.extension().and {it.installedBy == ManagedInstaller }
                .test(extension(Sample, ManagedInstaller))
        !Disables.extension().and {it.installedBy == HealthCheckInstaller }
                .test(extension(Sample, ManagedInstaller))
        
        !Disables.installedBy(HealthCheckInstaller).test(extension(Sample, ManagedInstaller))
        Disables.installedBy(HealthCheckInstaller).test(extension(Sample, HealthCheckInstaller))

        Disables.webExtension().test(extension(Sample, WebFilterInstaller))
        !Disables.webExtension().test(extension(Sample, ManagedInstaller))
        Disables.jerseyExtension().test(extension(Sample, ResourceInstaller))
        !Disables.jerseyExtension().test(extension(Sample, ManagedInstaller))

        !Disables.module().and {it.overriding}.test(module())
        Disables.module().and {it.overriding}.test(module(true))

        !Disables.bundle().and {it.fromLookup}.test(bundle(Sample))
        Disables.bundle().and {it.fromLookup}.test(bundle(Sample, true))

        Disables.dropwizardBundle().and {it.enabled}.test(dropwizardBundle(Sample))
        !Disables.dropwizardBundle().and {it.enabled}.test(dropwizardBundle(Sample, true))

        Disables.installer().and {it.enabled}.test(installer(Sample))
        !Disables.installer().and {it.enabled}.test(installer(Sample, true))
    }

    def "Check composition"() {

        def predicate = Disables.registeredBy(Application)
                .and(Disables.installer())
                .and(Disables.type(Sample2).negate())

        expect:
        predicate.test(item(Installer, Sample))
        !predicate.test(item(Extension, Sample))
        !predicate.test(item(Installer, Sample2))
        !predicate.test(item(Installer, Sample, Sample))
    }

    ItemInfo item(ConfigItem type, Class cls, Class from = Application) {
        ItemInfoImpl info = new ItemInfoImpl(type, ItemId.from(cls))
        info.countRegistrationAttempt(ItemId.from(from))
        return info;
    }

    ExtensionItemInfo extension(Class type, Class installer) {
        ExtensionItemInfoImpl res = new ExtensionItemInfoImpl(type)
        res.setInstaller(installer.constructors[0].newInstance() as FeatureInstaller)
        return res
    }

    ModuleItemInfo module(boolean overriding = false) {
        Closure<ModuleItemInfo> create = {
            new ModuleItemInfoImpl(new Module() {
                @Override
                void configure(Binder binder) {
                }
            })
        }
        ModuleItemInfo res

        if (overriding) {
            ModuleItemInfoImpl.overrideScope {
                res = create()
            }
        } else {
            res = create()
        }

        return res
    }

    GuiceyBundleItemInfo bundle(Class type, boolean fromLookup = false) {
        GuiceyBundleItemInfoImpl res = new GuiceyBundleItemInfoImpl(type)
        if (fromLookup) {
            res.countRegistrationAttempt(ConfigScope.BundleLookup.key)
        }
        return res
    }

    DropwizardBundleItemInfo dropwizardBundle(Class type, boolean disabled = false) {
        DropwizardBundleItemInfoImpl res = new DropwizardBundleItemInfoImpl(type)
        if (disabled) {
            res.disabledBy.add(ConfigScope.GuiceyBundle.key)
        }
        return res
    }

    InstallerItemInfo installer(Class type, boolean disabled = false) {
        InstallerItemInfoImpl res = new InstallerItemInfoImpl(type)
        if (disabled) {
            res.disabledBy.add(ConfigScope.GuiceyBundle.key)
        }
        return res
    }


    static class Sample {}

    static class Sample2 {}
}
