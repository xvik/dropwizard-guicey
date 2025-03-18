package ru.vyarus.dropwizard.guice.module.installer.bundle;

import com.google.common.base.Preconditions;
import com.google.inject.Module;
import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.ConfiguredBundle;
import io.dropwizard.core.setup.Bootstrap;
import ru.vyarus.dropwizard.guice.module.context.ConfigurationContext;
import ru.vyarus.dropwizard.guice.module.context.option.Option;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.util.BundleSupport;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleListener;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Guicey initialization object. Provides almost the same configuration methods as
 * {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder}. Also, contains dropwizard bootstrap objects.
 * May register pure dropwizard bundles.
 * <p>
 * In contrast to main builder, guicey bundle can't:
 * <ul>
 * <li>Disable bundles (because at this stage bundles already partly processed)</li>
 * <li>Use generic disable predicates (to not allow bundles disable, moreover it's tests-oriented feature)</li>
 * <li>Change options (because some bundles may already apply configuration based on changed option value
 * which will mean inconsistent state)</li>
 * <li>Register listener, implementing {@link ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook}
 * (because it's too late - all hooks were processed)</li>
 * <li>Register some special objects like custom injector factory or custom bundles lookup</li>
 * </ul>
 *
 * @author Vyacheslav Rusakov
 * @since 01.08.2015
 */
@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public class GuiceyBootstrap implements GuiceyCommonRegistration<GuiceyBootstrap> {

    private final ConfigurationContext context;
    // path for tracking bundles installation loops (a bundle registers another bundle and so on)
    // managed outside the bootstrap object, but need the reference for transitive bundles registration
    private final List<Class<? extends GuiceyBundle>> bundlesPath;
    private final List<GuiceyBundle> initOrder;

    public GuiceyBootstrap(final ConfigurationContext context,
                           final List<Class<? extends GuiceyBundle>> bundlesPath,
                           final List<GuiceyBundle> initOrder) {
        this.context = context;
        this.bundlesPath = bundlesPath;
        this.initOrder = initOrder;
    }

    /**
     * If bundle provides new installers then they must be declared here.
     * Optionally, core or other 3rd party installers may be declared also to indicate dependency
     * (duplicate installers registrations will be removed).
     *
     * @param installers feature installer classes to register
     * @return bootstrap instance for chained calls
     * @see ru.vyarus.dropwizard.guice.GuiceBundle.Builder#installers(Class[])
     */
    @SafeVarargs
    public final GuiceyBootstrap installers(final Class<? extends FeatureInstaller>... installers) {
        context.registerInstallers(installers);
        return this;
    }

    /**
     * Register other guicey bundles for installation. Bundles initialized immediately (same as transitive dropwizard
     * bundles and guice modules).
     * <p>
     * Equal instances of the same type will be considered as duplicate.
     *
     * @param bundles guicey bundles
     * @return bootstrap instance for chained calls
     * @see ru.vyarus.dropwizard.guice.GuiceBundle.Builder#bundles(GuiceyBundle...)
     */
    public GuiceyBootstrap bundles(final GuiceyBundle... bundles) {
        // immediate registration (same as for dropwizard bundles and guice modules)
        BundleSupport.initBundles(context, this, bundlesPath, initOrder, context.registerBundles(bundles));
        return this;
    }

    /**
     * Shortcut for dropwizard bundles registration (instead of {@code bootstrap().addBundle()}), but with
     * duplicates detection and tracking in diagnostic reporting. Dropwizard bundle is immediately initialized.
     *
     * @param bundles dropwizard bundles to register
     * @return bootstrap instance for chained calls
     * @see ru.vyarus.dropwizard.guice.GuiceBundle.Builder#dropwizardBundles(ConfiguredBundle...)
     */
    public GuiceyBootstrap dropwizardBundles(final ConfiguredBundle... bundles) {
        context.registerDropwizardBundles(bundles);
        return this;
    }

    /**
     * @param installers feature installer types to disable
     * @return bootstrap instance for chained calls
     * @see ru.vyarus.dropwizard.guice.GuiceBundle.Builder#disableInstallers(Class[])
     */
    @SafeVarargs
    public final GuiceyBootstrap disableInstallers(final Class<? extends FeatureInstaller>... installers) {
        context.disableInstallers(installers);
        return this;
    }

    // ------------------------------------------------------------------ COMMON METHODS

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <K extends Configuration> Bootstrap<K> bootstrap() {
        return context.getBootstrap();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <K extends Configuration> Application<K> application() {
        return context.getBootstrap().getApplication();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <V, K extends Enum<? extends Option> & Option> V option(final K option) {
        return context.option(option);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GuiceyBootstrap modules(final Module... modules) {
        Preconditions.checkState(modules.length > 0, "Specify at least one module");
        context.registerModules(modules);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GuiceyBootstrap modulesOverride(final Module... modules) {
        context.registerModulesOverride(modules);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GuiceyBootstrap extensions(final Class<?>... extensionClasses) {
        context.registerExtensions(extensionClasses);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GuiceyBootstrap extensionsOptional(final Class<?>... extensionClasses) {
        context.registerExtensionsOptional(extensionClasses);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GuiceyBootstrap disableExtensions(final Class<?>... extensions) {
        context.disableExtensions(extensions);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SafeVarargs
    public final GuiceyBootstrap disableModules(final Class<? extends Module>... modules) {
        context.disableModules(modules);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GuiceyBootstrap listen(final GuiceyLifecycleListener... listeners) {
        context.lifecycle().register(listeners);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <K> GuiceyBootstrap shareState(final Class<K> key, final K value) {
        context.getSharedState().put(key, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <K> K sharedState(final Class<K> key, final Supplier<K> defaultValue) {
        return context.getSharedState().get(key, defaultValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <K> Optional<K> sharedState(final Class<K> key) {
        return Optional.ofNullable(context.getSharedState().get(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <K> K sharedStateOrFail(final Class<K> key, final String message, final Object... args) {
        return context.getSharedState().getOrFail(key, message, args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <V> void whenSharedStateReady(final Class<V> key, final Consumer<V> action) {
        context.getSharedState().whenReady(key, action);
    }
}
