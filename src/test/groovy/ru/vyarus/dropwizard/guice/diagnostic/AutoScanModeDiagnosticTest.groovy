package ru.vyarus.dropwizard.guice.diagnostic

import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.diagnostic.support.AutoScanApp
import ru.vyarus.dropwizard.guice.diagnostic.support.features.FooInstaller
import ru.vyarus.dropwizard.guice.diagnostic.support.features.FooModule
import ru.vyarus.dropwizard.guice.diagnostic.support.features.FooResource
import ru.vyarus.dropwizard.guice.module.GuiceSupportModule
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.installer.CoreInstallersBundle
import ru.vyarus.dropwizard.guice.module.installer.feature.LifeCycleInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.ManagedInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.TaskInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.admin.AdminFilterInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.admin.AdminServletInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.eager.EagerSingletonInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.health.HealthCheckInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.JerseyFeatureInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.ResourceInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.provider.JerseyProviderInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.plugin.PluginInstaller
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 26.06.2016
 */
@UseGuiceyApp(AutoScanApp)
class AutoScanModeDiagnosticTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info

    def "Check diagnostic info correctness"() {

        expect: "correct bundles info"
        info.bundles == [CoreInstallersBundle]
        info.bundlesFromLookup.isEmpty()

        and: "correct installers info"
        def classes = [FooInstaller,
                       ManagedInstaller.class,
                       JerseyFeatureInstaller.class,
                       JerseyProviderInstaller.class,
                       ResourceInstaller.class,
                       EagerSingletonInstaller.class,
                       HealthCheckInstaller.class,
                       TaskInstaller.class,
                       PluginInstaller.class,
                       AdminFilterInstaller.class,
                       AdminServletInstaller.class]
        info.installers as Set == classes  as Set
        info.installersDisabled == [LifeCycleInstaller]
        info.installersFromScan == [FooInstaller]

        and: "correct extensions info"
        info.extensions == [FooResource]
        info.extensionsFromScan == [FooResource]
        info.getExtensions(ResourceInstaller) == [FooResource]

        and: "correct modules"
        info.modules as Set == [FooModule, GuiceSupportModule] as Set

    }
}
