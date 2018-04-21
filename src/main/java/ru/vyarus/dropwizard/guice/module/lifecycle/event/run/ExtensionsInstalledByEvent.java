package ru.vyarus.dropwizard.guice.module.lifecycle.event.run;

import com.google.inject.Injector;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.module.context.option.OptionsInfo;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.InjectorPhaseEvent;

import java.util.List;

/**
 * Called when installer installed all related extensions and only for installers actually performed
 * installations (extensions list never empty). Provides installer and installed extensions types.
 * <p>
 * NOTE: {@link ru.vyarus.dropwizard.guice.module.installer.install.JerseyInstaller} installers will no be
 * notified here, even if they participate in installation as installation is considered incomplete at that point.
 * <p>
 * Extension instance could be obtained manually from injector.
 * <p>
 * Installer passed by type to simplify differentiation. If, for some reason, you need to access installer
 * instance then save installers in {@link InstallersResolvedEvent} and find by type (only one installer of
 * exact type could be registered).
 *
 * @author Vyacheslav Rusakov
 * @since 19.04.2018
 */
public class ExtensionsInstalledByEvent extends InjectorPhaseEvent {

    private final Class<? extends FeatureInstaller> installer;
    private final List<Class<?>> installed;

    public ExtensionsInstalledByEvent(final OptionsInfo options,
                                      final Bootstrap bootstrap,
                                      final Configuration configuration,
                                      final Environment environment,
                                      final Injector injector,
                                      final Class<? extends FeatureInstaller> installer,
                                      final List<Class<?>> installed) {
        super(GuiceyLifecycle.ExtensionsInstalledBy, options, bootstrap, configuration, environment, injector);
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
     * @return installed extension types
     */
    public List<Class<?>> getInstalled() {
        return installed;
    }
}
