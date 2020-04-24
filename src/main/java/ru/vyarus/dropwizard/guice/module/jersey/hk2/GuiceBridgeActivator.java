package ru.vyarus.dropwizard.guice.module.jersey.hk2;


/**
 * Enables HK2 guice bridge to allow HK2 services to see guice beans. Bridge installation is enabled by
 * {@link ru.vyarus.dropwizard.guice.GuiceyOptions#UseHkBridge} option.
 * <p>
 * Installation extracted to separate class to isolate bridge dependency usage: when bridge is enabled.
 * extra 'org.glassfish.hk2:guice-bridge:2.6.1' dependency is required.
 *
 * @author Vyacheslav Rusakov
 * @since 26.03.2017
 */
public class GuiceBridgeActivator {

//    private final InjectionManager injectionManager;
//    private final Injector injector;
//
//    public GuiceBridgeActivator(final InjectionManager injectionManager, final Injector injector) {
//        this.injectionManager = injectionManager;
//        this.injector = injector;
//    }

    /**
     * Activate HK2 guice bridge.
     */
    public void activate() {
//        final ServiceLocator locator = injectionManager.getInstance(ServiceLocator.class);
//        GuiceBridge.getGuiceBridge().initializeGuiceBridge(locator);
//        final GuiceIntoHK2Bridge guiceBridge = injectionManager.getInstance(GuiceIntoHK2Bridge.class);
//        guiceBridge.bridgeGuiceInjector(injector);
    }
}
