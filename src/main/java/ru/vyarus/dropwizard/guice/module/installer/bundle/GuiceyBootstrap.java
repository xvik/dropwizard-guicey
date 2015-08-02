package ru.vyarus.dropwizard.guice.module.installer.bundle;

import com.google.common.base.Preconditions;
import com.google.inject.Module;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.internal.InstallerConfig;

import java.util.Arrays;
import java.util.List;

/**
 * Guicey configuration object. Provides almost the same configuration methods as
 * {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder}. Also, contains dropwizard configuration and
 * environment objects (in case if they are required).
 *
 * @author Vyacheslav Rusakov
 * @since 01.08.2015
 */
@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public class GuiceyBootstrap {

    private final List<Module> modules;
    private final InstallerConfig installerConfig;
    private final Configuration configuration;
    private final Environment environment;

    public GuiceyBootstrap(final List<Module> modules, final InstallerConfig installerConfig,
                           final Configuration configuration, final Environment environment) {
        this.modules = modules;
        this.installerConfig = installerConfig;
        this.configuration = configuration;
        this.environment = environment;
    }

    /**
     * @param <T> configuration type
     * @return configuration instance
     */
    @SuppressWarnings("unchecked")
    public <T extends Configuration> T configuration() {
        return (T) configuration;
    }

    /**
     * @return environment instance
     */
    public Environment environment() {
        return environment;
    }

    /**
     * NOTE: if module implements *AwareModule interfaces, objects will be set just before configuration start.
     *
     * @param modules one or more juice modules
     * @return configurer instance for chained calls
     * @see ru.vyarus.dropwizard.guice.module.support.BootstrapAwareModule
     * @see ru.vyarus.dropwizard.guice.module.support.ConfigurationAwareModule
     * @see ru.vyarus.dropwizard.guice.module.support.EnvironmentAwareModule
     * @see ru.vyarus.dropwizard.guice.module.support.DropwizardAwareModule
     */
    public GuiceyBootstrap modules(final Module... modules) {
        Preconditions.checkState(modules.length > 0, "Specify at least one module");
        this.modules.addAll(Arrays.asList(modules));
        return this;
    }

    /**
     * @param installers feature installer types to disable
     * @return configurer instance for chained calls
     */
    @SafeVarargs
    public final GuiceyBootstrap disableInstallers(final Class<? extends FeatureInstaller>... installers) {
        installerConfig.getDisabledFeatures().addAll(Arrays.asList(installers));
        return this;
    }

    /**
     * Feature installers registered automatically when auto scan enabled,
     * but if you don't want to use it, you can register installers manually (note: without auto scan default
     * installers will not be registered).
     * <p>Also, could be used to add installers from packages not included in auto scanning.</p>
     *
     * @param installers feature installer classes to register
     * @return configurer instance for chained calls
     */
    @SafeVarargs
    public final GuiceyBootstrap installers(final Class<? extends FeatureInstaller>... installers) {
        installerConfig.getManualFeatures().addAll(Arrays.asList(installers));
        return this;
    }

    /**
     * Beans could be registered automatically when auto scan enabled,
     * but if you don't want to use it, you can register beans manually.
     * <p>Guice injector will instantiate beans and registered installers will be used to recognize and
     * properly register provided extension beans.</p>
     * <p>Also, could be used to add beans from packages not included in auto scanning.</p>
     * <p>NOTE: startup will fail if bean not recognized by installers.</p>
     * <p>NOTE: Don't register commands here: either enable auto scan, which will install commands automatically
     * or register command directly to bootstrap object and dependencies will be injected to them after
     * injector creation.</p>
     *
     * @param extensionClasses extension bean classes to register
     * @return configurer instance for chained calls
     */
    public GuiceyBootstrap extensions(final Class<?>... extensionClasses) {
        installerConfig.getManualBeans().addAll(Arrays.asList(extensionClasses));
        return this;
    }
}
