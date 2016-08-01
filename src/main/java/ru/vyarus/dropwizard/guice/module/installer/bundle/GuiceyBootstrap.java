package ru.vyarus.dropwizard.guice.module.installer.bundle;

import com.google.common.base.Preconditions;
import com.google.inject.Module;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.module.context.ConfigurationContext;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;

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

    private final ConfigurationContext context;
    private final List<GuiceyBundle> iterationBundles;
    private final Configuration configuration;
    private final Environment environment;
    private final Application application;

    public GuiceyBootstrap(final ConfigurationContext context, final List<GuiceyBundle> iterationBundles,
                           final Configuration configuration, final Environment environment,
                           final Application application) {
        this.context = context;
        this.iterationBundles = iterationBundles;
        this.configuration = configuration;
        this.environment = environment;
        this.application = application;
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
     * Application instance may be useful for complex (half manual) integrations where access for
     * injector is required.
     * For example, manually registered
     * {@link io.dropwizard.lifecycle.Managed} may access injector in it's start method by calling
     * {@link ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup#getInjector(Application)}.
     * <p>
     * NOTE: it will work in this example, because injector access will be after injector creation.
     * Directly inside bundle initialization method injector could not be obtained as it's not exists yet.
     *
     * @return dropwizard application instance
     */
    public Application application() {
        return application;
    }

    /**
     * All registered modules must be of unique type (all modules registered). If two or more modules of the
     * same type registered, only first instance will be used.
     * <p>
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
        context.registerModules(modules);
        return this;
    }

    /**
     * @param installers feature installer types to disable
     * @return configurer instance for chained calls
     */
    @SafeVarargs
    public final GuiceyBootstrap disableInstallers(final Class<? extends FeatureInstaller>... installers) {
        context.disableInstallers(installers);
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
        context.registerInstallers(installers);
        return this;
    }

    /**
     * Bundle should not rely on auto-scan mechanism and so must declare all extensions manually
     * (this better declares bundle content and speed ups startup).
     * <p>
     * NOTE: startup will fail if bean not recognized by installers.
     * <p>
     * NOTE: Don't register commands here: either enable auto scan, which will install commands automatically
     * or register command directly to bootstrap object and dependencies will be injected to them after
     * injector creation.
     *
     * @param extensionClasses extension bean classes to register
     * @return configurer instance for chained calls
     */
    public GuiceyBootstrap extensions(final Class<?>... extensionClasses) {
        context.registerExtensions(extensionClasses);
        return this;
    }

    /**
     * Register other guicey bundles for installation.
     * <p>
     * Duplicate bundles will be filtered automatically: bundles of the same type considered duplicate
     * (if two or more bundles of the same type detected then only first instance will be processed).
     *
     * @param bundles guicey bundles
     * @return configurer instance for chained calls
     */
    public GuiceyBootstrap bundles(final GuiceyBundle... bundles) {
        context.registerBundles(bundles);
        iterationBundles.addAll(Arrays.asList(bundles));
        return this;
    }
}
