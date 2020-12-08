package ru.vyarus.dropwizard.guice.module.context.bootstrap;

import io.dropwizard.Application;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;
import ru.vyarus.dropwizard.guice.module.context.ConfigurationContext;

/**
 * {@link Bootstrap} proxy delegates all calls directly to bootstrap object, except bundle addition. Instead,
 * bundle addition is replaced with guicey bundle registration so disable and deduplication rules could be
 * applied for transitive dropwizard bundles.
 *
 * @author Vyacheslav Rusakov
 * @since 07.05.2019
 */
public final class BootstrapProxyFactory {

    private BootstrapProxyFactory() {
    }

    /**
     * @param bootstrap dropwizard bootstrap object
     * @param context   guicey configuration context
     * @return dropwizard bootstrap proxy object
     */
    @SuppressWarnings("unchecked")
    public static Bootstrap create(final Bootstrap bootstrap, final ConfigurationContext context) {
        try {
            final ProxyFactory factory = new ProxyFactory();
            factory.setSuperclass(Bootstrap.class);
            final Class proxy = factory.createClass();

            final Bootstrap res = (Bootstrap) proxy.getConstructor(Application.class).newInstance(new Object[]{null});
            ((Proxy) res).setHandler((self, thisMethod, proceed, args) -> {
                // intercept only bundle addition
                if ("addBundle".equals(thisMethod.getName())) {
                    context.registerDropwizardBundles((ConfiguredBundle) args[0]);
                    return null;
                }
                // other methods called as is
                return thisMethod.invoke(bootstrap, args);
            });
            return res;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create Bootstrap proxy", e);
        }
    }
}
