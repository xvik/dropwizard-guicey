package ru.vyarus.dropwizard.guice.module.jersey;

import com.google.common.base.Preconditions;
import com.google.inject.Injector;
import org.glassfish.jersey.InjectionManagerProvider;
import org.glassfish.jersey.internal.inject.InjectionManager;
import ru.vyarus.dropwizard.guice.module.context.stat.StatsTracker;
import ru.vyarus.dropwizard.guice.module.installer.scanner.InvisibleForScanner;
import ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding;
import ru.vyarus.dropwizard.guice.module.jersey.hk2.GuiceBridgeActivator;
import ru.vyarus.dropwizard.guice.module.jersey.hk2.InstallerBinder;
import ru.vyarus.dropwizard.guice.module.lifecycle.internal.LifecycleSupport;

import jakarta.inject.Provider;
import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;

import static ru.vyarus.dropwizard.guice.module.context.stat.Stat.JerseyTime;

/**
 * Feature activates guice integration.
 * <p>
 * Guice context is created first and it doesn't depend on jersey start. First of all this allow using guice
 * in commands and second, guice is ready in time of jersey initialization and so can provide it's own instances
 * into jersey config.
 * <p>
 * Feature must be registered in jersey before it's start:
 * {@code environment.jersey().register(new GuiceFeature())}
 * <p>
 * During guice context start special jersey bindings module registered
 * {@link ru.vyarus.dropwizard.guice.module.jersey.hk2.GuiceBindingsModule}, which lazily binds jersey specific
 * types to guice context. This types could be used in guice only after actual integration
 * (this feature activation)
 * <p>
 * HK2-guice bridge is activated when {@link ru.vyarus.dropwizard.guice.GuiceyOptions#UseHkBridge} option enabled
 * (not bi-directional). By default, it's disabled because most cases does not require it: it was
 * developed for cases when bean is created by HK2 and only need some injections from guice, but here guice
 * controls almost everything and prepared instance is passed to guice. But bridge may be useful together with
 * {@link ru.vyarus.dropwizard.guice.module.installer.feature.jersey.JerseyManaged} instances.
 * <p>
 * Feature installs {@code ru.vyarus.dropwizard.guice.module.jersey.hk2.InstallerBinder}, which is HK2 module.
 * Just like with guice ({@code BindingInstaller)}, it asks all {@code JerseyInstaller} to bind extensions into
 * HK2 context.
 *
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.dropwizard.guice.module.jersey.support.JerseyComponentProvider
 * @see ru.vyarus.dropwizard.guice.module.jersey.support.GuiceComponentFactory
 * @see ru.vyarus.dropwizard.guice.module.jersey.support.LazyGuiceFactory
 * @since 21.11.2014
 */
@InvisibleForScanner
public class GuiceFeature implements Feature, Provider<InjectionManager> {

    private final Provider<Injector> provider;
    private final StatsTracker tracker;
    private final LifecycleSupport lifecycle;
    private final boolean enableBridge;
    private InjectionManager injectionManager;

    /**
     * Create feature.
     *
     * @param provider     injector provider
     * @param tracker      tracker
     * @param lifecycle    listeners support
     * @param enableBridge true to enable hk guice bridge
     */
    public GuiceFeature(final Provider<Injector> provider, final StatsTracker tracker,
                        final LifecycleSupport lifecycle, final boolean enableBridge) {
        this.provider = provider;
        this.tracker = tracker;
        this.lifecycle = lifecycle;
        this.enableBridge = enableBridge;
    }

    @Override
    public boolean configure(final FeatureContext context) {
        tracker.startJerseyTimer(JerseyTime);
        injectionManager = InjectionManagerProvider.getInjectionManager(context);
        lifecycle.jerseyConfiguration(injectionManager);
        final Injector injector = this.provider.get();

        if (enableBridge) {
            Preconditions.checkState(JerseyBinding.isBridgeAvailable(),
                    "HK2 bridge is requested, but dependency not found: "
                            + "'org.glassfish.hk2:guice-bridge:2.6.1' (check that dependency "
                            + "version match HK2 version used in your dropwizard)");
            new GuiceBridgeActivator(injectionManager, injector).activate();
        }

        context.register(new InstallerBinder(injector, tracker, lifecycle));
        tracker.stopJerseyTimer(JerseyTime);
        return true;
    }

    @Override
    public InjectionManager get() {
        return Preconditions.checkNotNull(injectionManager, "Jersey InjectionManager is not yet available");
    }
}
