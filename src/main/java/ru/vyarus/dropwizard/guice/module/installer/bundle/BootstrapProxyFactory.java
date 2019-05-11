package ru.vyarus.dropwizard.guice.module.installer.bundle;

import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import java.util.function.Supplier;

/**
 * @author Vyacheslav Rusakov
 * @since 07.05.2019
 */
public class BootstrapProxyFactory {

    public static <T extends Configuration> Bootstrap<T> proxy(final Bootstrap<T> bootstrap) {
        ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(Bootstrap.class);

        Class proxy = factory.createClass();

        ProxySupplier<T> supplier = new ProxySupplier<>();
        MethodHandler handler = (self, overridden, forwarder, args) -> {
            if (overridden.getName().equals("addBundle")) {
                ConfiguredBundle bundle = (ConfiguredBundle) args[0];
                bootstrap.addBundle(bundle instanceof DropwizardBundleTracker
                        ? bundle : new DropwizardBundleTracker<>(bundle, supplier));
            }
            return forwarder.invoke(bootstrap, args);
        };
        Bootstrap<T> instance;
        try {
            instance = (Bootstrap<T>) proxy.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create Bootstrap proxy", e);
        }
        supplier.bootstrap = instance;
        ((ProxyObject) instance).setHandler(handler);
        return instance;
    }


    private static class ProxySupplier<T extends Configuration> implements Supplier<Bootstrap<T>> {
        Bootstrap<T> bootstrap;

        @Override
        public Bootstrap<T> get() {
            return bootstrap;
        }
    }
}
