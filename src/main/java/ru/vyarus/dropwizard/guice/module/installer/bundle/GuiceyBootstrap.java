package ru.vyarus.dropwizard.guice.module.installer.bundle;

import com.google.common.base.Preconditions;
import com.google.inject.Module;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;

import java.util.Arrays;

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

    private final BundleContext context;
    private final Configuration configuration;
    private final Environment environment;

    public GuiceyBootstrap(final BundleContext context,
                           final Configuration configuration, final Environment environment) {
        this.context = context;
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
        context.modules.addAll(Arrays.asList(modules));
        return this;
    }

    /**
     * @param installers feature installer types to disable
     * @return configurer instance for chained calls
     */
    @SafeVarargs
    public final GuiceyBootstrap disableInstallers(final Class<? extends FeatureInstaller>... installers) {
        context.installerConfig.getDisabledInstallers().addAll(Arrays.asList(installers));
        return this;
    }

    /**
     * If bundle provides new installers then they must be declared here.
     * Optionally, core or other 3rd party installers may be declared also to indicate dependency
     * (duplicate installers registrations will be removed).
     *
     * @param installers feature installer classes to register
     * @return configurer instance for chained calls
     */
    @SafeVarargs
    public final GuiceyBootstrap installers(final Class<? extends FeatureInstaller>... installers) {
        context.installerConfig.getManualInstallers().addAll(Arrays.asList(installers));
        return this;
    }

    /**
     * Bundle should not rely on auto-scan mechanism and so must declare all extensions manually
     * (this better declares bundle content and speed ups startup).
     * <p/>
     * NOTE: startup will fail if bean not recognized by installers.
     * <p/>
     * NOTE: Don't register commands here: either enable auto scan, which will install commands automatically
     * or register command directly to bootstrap object and dependencies will be injected to them after
     * injector creation.
     *
     * @param extensionClasses extension bean classes to register
     * @return configurer instance for chained calls
     */
    public GuiceyBootstrap extensions(final Class<?>... extensionClasses) {
        context.installerConfig.getManualExtensions().addAll(Arrays.asList(extensionClasses));
        return this;
    }

    /**
     * Register other guicey bundles for installation.
     * <p/>
     * Duplicate bundles will be filtered automatically: bundles of the same type considered duplicate
     * (if two or more bundles of the same type detected then only first instance will be processed).
     *
     * @param bundles guicey bundles
     * @return configurer instance for chained calls
     */
    public GuiceyBootstrap bundles(final GuiceyBundle... bundles) {
        context.bundles.addAll(Arrays.asList(bundles));
        return this;
    }
}
