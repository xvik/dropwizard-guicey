package ru.vyarus.dropwizard.guice.module.installer;

import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap;
import ru.vyarus.dropwizard.guice.module.context.unique.item.UniqueGuiceyBundle;
import ru.vyarus.dropwizard.guice.module.installer.feature.LifeCycleInstaller;
import ru.vyarus.dropwizard.guice.module.installer.feature.ManagedInstaller;
import ru.vyarus.dropwizard.guice.module.installer.feature.TaskInstaller;
import ru.vyarus.dropwizard.guice.module.installer.feature.eager.EagerSingletonInstaller;
import ru.vyarus.dropwizard.guice.module.installer.feature.health.HealthCheckInstaller;
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.JerseyFeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.ResourceInstaller;
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.provider.JerseyProviderInstaller;
import ru.vyarus.dropwizard.guice.module.installer.feature.plugin.PluginInstaller;

/**
 * Core installers bundle. Installs {@link WebInstallersBundle}.
 *
 * @author Vyacheslav Rusakov
 * @since 02.08.2015
 */
public class CoreInstallersBundle extends UniqueGuiceyBundle {

    @Override
    public void initialize(final GuiceyBootstrap bootstrap) {
        bootstrap.installers(
                LifeCycleInstaller.class,
                ManagedInstaller.class,
                JerseyFeatureInstaller.class,
                JerseyProviderInstaller.class,
                ResourceInstaller.class,
                EagerSingletonInstaller.class,
                HealthCheckInstaller.class,
                TaskInstaller.class,
                PluginInstaller.class
        );
        bootstrap.bundles(new WebInstallersBundle());
    }
}
