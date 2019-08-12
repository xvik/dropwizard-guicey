package ru.vyarus.dropwizard.guice.module.context.bootstrap;

import io.dropwizard.Application;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import org.assertj.core.internal.bytebuddy.ByteBuddy;
import org.assertj.core.internal.bytebuddy.description.modifier.Visibility;
import org.assertj.core.internal.bytebuddy.implementation.InvocationHandlerAdapter;
import org.assertj.core.internal.bytebuddy.implementation.MethodCall;
import org.assertj.core.internal.bytebuddy.matcher.ElementMatchers;
import ru.vyarus.dropwizard.guice.module.context.ConfigurationContext;

/**
 * {@link Bootstrap} proxy delegates all calls directly to bootstrap object, except bundle addition. Instead,
 * bundle addition is replaced with guicey bundle registration so disable and deduplication rules could be
 * applied for transitive dropwizard bundles.
 * <p>
 * Bootstrap proxy object creation adds ~200ms startup overhead, which is clearly visible on
 * stats diagnostic report. Class directly extending {@link Bootstrap} would be much more effective, but it would
 * require overriding of all methods and would be very vulnerable for future bootstrap object changes.
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
    public static Bootstrap create(final Bootstrap bootstrap, final ConfigurationContext context) {
        try {
            return new ByteBuddy()
                    .subclass(Bootstrap.class)
                    .defineConstructor(Visibility.PUBLIC)
                    .intercept(
                            MethodCall.invoke(Bootstrap.class.getDeclaredConstructor(Application.class))
                                    .with(new Object[]{null}))
                    .method(ElementMatchers.any())
                    .intercept(InvocationHandlerAdapter.of((proxy, method, args) -> {
                        // intercept only bundle addition
                        if (method.getName().equals("addBundle")) {
                            context.registerDropwizardBundles((ConfiguredBundle) args[0]);
                            return null;
                        }
                        // other methods called as is
                        return method.invoke(bootstrap, args);
                    }))
                    .make()
                    .load(Bootstrap.class.getClassLoader())
                    .getLoaded()
                    .newInstance();

        } catch (Exception e) {
            throw new IllegalStateException("Failed to create Bootstrap proxy", e);
        }
    }
}
