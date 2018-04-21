package ru.vyarus.dropwizard.guice.module.lifecycle.event.hk;

import com.google.inject.Injector;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.hk2.api.ServiceLocator;
import ru.vyarus.dropwizard.guice.module.context.option.OptionsInfo;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.HkPhaseEvent;

import java.util.List;

/**
 * Called when {@link ru.vyarus.dropwizard.guice.module.installer.install.JerseyInstaller} installer installed all
 * related extensions and only for installers actually performed installations (extensions list never empty).
 * Provides installer and installed extensions types.
 * <p>
 * At this point hk is not completely started and so hk managed extensions
 * ({@link ru.vyarus.dropwizard.guice.module.installer.feature.jersey.HK2Managed}) couldn't be obtained yet
 * (even though you have access to root service locator). But extensions managed by guice could be obtained
 * from guice context.
 * <p>
 * Installer passed by type to simplify differentiation. If, for some reason, you need to access installer
 * instance then save installers in
 * {@link ru.vyarus.dropwizard.guice.module.lifecycle.event.run.InstallersResolvedEvent} and find by type
 * (only one installer of exact type could be registered).
 *
 * @author Vyacheslav Rusakov
 * @since 19.04.2018
 */
public class HkExtensionsInstalledByEvent extends HkPhaseEvent {

    private final Class<? extends FeatureInstaller> installer;
    private final List<Class<?>> installed;

    @SuppressWarnings("ParameterNumber")
    public HkExtensionsInstalledByEvent(final OptionsInfo options,
                                        final Bootstrap bootstrap,
                                        final Configuration configuration,
                                        final Environment environment,
                                        final Injector injector,
                                        final ServiceLocator locator,
                                        final Class<? extends FeatureInstaller> installer,
                                        final List<Class<?>> installed) {
        super(GuiceyLifecycle.HkExtensionsInstalledBy, options, bootstrap, configuration, environment,
                injector, locator);
        this.installer = installer;
        this.installed = installed;
    }

    /**
     * @return installer type
     */
    public Class<? extends FeatureInstaller> getInstaller() {
        return installer;
    }

    /**
     * @return installed extensions type
     */
    public List<Class<?>> getInstalled() {
        return installed;
    }
}
