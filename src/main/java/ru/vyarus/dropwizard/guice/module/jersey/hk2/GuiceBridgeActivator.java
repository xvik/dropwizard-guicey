package ru.vyarus.dropwizard.guice.module.jersey.hk2;

import com.google.inject.Injector;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.guice.bridge.api.GuiceBridge;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;

/**
 * Enables HK2 guice bridge to allow HK2 services to see guice beans. Bridge installation is enabled by
 * {@link ru.vyarus.dropwizard.guice.GuiceyOptions#UseHkBridge} option.
 * <p>
 * Installation extracted to separate class to isolate bridge dependency usage: when bridge is enabled.
 * extra 'org.glassfish.hk2:guice-bridge:2.5.0' dependency is required.
 *
 * @author Vyacheslav Rusakov
 * @since 26.03.2017
 */
public class GuiceBridgeActivator {

    private final ServiceLocator locator;
    private final Injector injector;

    public GuiceBridgeActivator(final ServiceLocator locator, final Injector injector) {
        this.locator = locator;
        this.injector = injector;
    }

    /**
     * Activate HK2 guice bridge.
     */
    public void activate() {
        GuiceBridge.getGuiceBridge().initializeGuiceBridge(locator);
        final GuiceIntoHK2Bridge guiceBridge = locator.getService(GuiceIntoHK2Bridge.class);
        guiceBridge.bridgeGuiceInjector(injector);
    }
}
