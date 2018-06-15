package ru.vyarus.dropwizard.guice.module.jersey;

import com.google.common.base.Preconditions;
import com.google.inject.Injector;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.ServiceLocatorProvider;
import ru.vyarus.dropwizard.guice.module.context.stat.StatsTracker;
import ru.vyarus.dropwizard.guice.module.installer.scanner.InvisibleForScanner;
import ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding;
import ru.vyarus.dropwizard.guice.module.jersey.hk2.GuiceBridgeActivator;
import ru.vyarus.dropwizard.guice.module.jersey.hk2.InstallerBinder;
import ru.vyarus.dropwizard.guice.module.lifecycle.internal.LifecycleSupport;

import javax.inject.Provider;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import static ru.vyarus.dropwizard.guice.module.context.stat.Stat.HKTime;

/**
 * Feature activates guice integration.
 * <p>Guice context is created first and it doesn't depend on jersey start. First of all this allow using guice
 * in commands and second, guice is ready in time of jersey initialization and so can provide it's own instances
 * into jersey config.</p>
 * <p>Feature must be registered in jersey before it's start:
 * {@code environment.jersey().register(new GuiceFeature())}</p>
 * <p>During juice context start special jersey bindings module registered
 * {@link ru.vyarus.dropwizard.guice.module.jersey.hk2.GuiceBindingsModule}, which lazily binds jersey specific
 * types to guice context. This types could be used in guice only after actual integration
 * (this feature activation)</p>
 * <p>HK2-guice bridge is activated when {@link ru.vyarus.dropwizard.guice.GuiceyOptions#UseHkBridge} option enabled
 * (not bi-directional). By default, it's disabled because most cases does not require it: it was
 * developed for cases when bean is created by HK2 and only need some injections from guice, but here guice
 * controls almost everything and prepared instance is passed to guice. But bridge may be useful together with
 * {@link ru.vyarus.dropwizard.guice.module.installer.feature.jersey.HK2Managed} instances.</p>
 * <p>Feature installs {@code ru.vyarus.dropwizard.guice.module.jersey.hk2.InstallerBinder}, which is HK2 module.
 * Just like with guice ({@code BindingInstaller)}, it asks all {@code JerseyInstaller} to bind extensions into
 * HK2 context.</p>
 *
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.dropwizard.guice.module.jersey.support.JerseyComponentProvider
 * @see ru.vyarus.dropwizard.guice.module.jersey.support.GuiceComponentFactory
 * @see ru.vyarus.dropwizard.guice.module.jersey.support.LazyGuiceFactory
 * @since 21.11.2014
 */
@InvisibleForScanner
public class GuiceFeature implements Feature, Provider<ServiceLocator> {

    private final Provider<Injector> provider;
    private final StatsTracker tracker;
    private final LifecycleSupport lifecycle;
    private final boolean enableBridge;
    private ServiceLocator locator;

    public GuiceFeature(final Provider<Injector> provider, final StatsTracker tracker,
                        final LifecycleSupport lifecycle, final boolean enableBridge) {
        this.provider = provider;
        this.tracker = tracker;
        this.lifecycle = lifecycle;
        this.enableBridge = enableBridge;
    }

    @Override
    public boolean configure(final FeatureContext context) {
        tracker.startHkTimer(HKTime);
        locator = ServiceLocatorProvider.getServiceLocator(context);
        lifecycle.hkConfiguration(locator);
        final Injector injector = this.provider.get();

        if (enableBridge) {
            Preconditions.checkState(JerseyBinding.isBridgeAvailable(),
                    "HK2 bridge is requested, but dependency not found: "
                            + "'org.glassfish.hk2:guice-bridge:2.5.0-b32' (check that dependency "
                            + "version match HK2 version used in your dropwizard)");
            new GuiceBridgeActivator(locator, injector).activate();
        }

        context.register(new InstallerBinder(injector, tracker, lifecycle));
        tracker.stopHkTimer(HKTime);
        return true;
    }

    @Override
    public ServiceLocator get() {
        return Preconditions.checkNotNull(locator, "Service locator is not yet available");
    }
}
