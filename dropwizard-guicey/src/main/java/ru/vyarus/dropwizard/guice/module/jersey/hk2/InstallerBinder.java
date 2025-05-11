package ru.vyarus.dropwizard.guice.module.jersey.hk2;

import com.google.inject.Injector;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import ru.vyarus.dropwizard.guice.module.context.stat.StatsTracker;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.JerseyInstaller;
import ru.vyarus.dropwizard.guice.module.installer.internal.ExtensionsHolder;
import ru.vyarus.dropwizard.guice.module.lifecycle.internal.LifecycleSupport;

import java.util.ArrayList;
import java.util.List;

import static ru.vyarus.dropwizard.guice.module.context.stat.Stat.JerseyInstallerTime;

/**
 * HK2 module, which must be registered before HK2 context start (to properly bind resources).
 * Registers all guice extensions into HK2 context as factory, which delegates to guice provider.
 * Such bridging is required to delegate objects creation to guice and properly use guice scopes.
 * <p>If jersey extension is already a factory, then factory will be registered directly.</p>
 *
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.dropwizard.guice.module.installer.install.JerseyInstaller
 * @since 16.11.2014
 */
public class InstallerBinder extends AbstractBinder {

    private final Injector injector;
    private final StatsTracker tracker;
    private final LifecycleSupport lifecycle;

    /**
     * Create binder.
     *
     * @param injector  injector
     * @param tracker   tracker
     * @param lifecycle listeners support
     */
    public InstallerBinder(final Injector injector, final StatsTracker tracker,
                           final LifecycleSupport lifecycle) {
        this.injector = injector;
        this.tracker = tracker;
        this.lifecycle = lifecycle;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void configure() {
        tracker.startJerseyTimer(JerseyInstallerTime);
        final ExtensionsHolder holder = injector.getInstance(ExtensionsHolder.class);
        final List<Class<?>> allInstalled = new ArrayList<>();
        for (FeatureInstaller installer : holder.getInstallers()) {
            if (installer instanceof JerseyInstaller) {
                final List<Class<?>> features = holder.getExtensions(installer.getClass());
                if (features != null) {
                    for (Class<?> type : features) {
                        ((JerseyInstaller) installer).install(this, injector, type);
                    }
                    allInstalled.addAll(features);
                }
                installer.report();
                lifecycle.jerseyExtensionsInstalled(installer.getClass(), features);
            }
        }
        lifecycle.jerseyExtensionsInstalled(allInstalled);
        tracker.stopJerseyTimer(JerseyInstallerTime);
    }
}
