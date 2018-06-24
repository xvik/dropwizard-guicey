package ru.vyarus.dropwizard.guice.hook;

import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.test.GuiceyConfigurationRule;
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyConfiguration;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Extra configuration mechanism to apply external configuration to bundle builder after manual configuration in
 * application class.
 * <p>
 * Supposed to be used for integration tests.
 *
 * @author Vyacheslav Rusakov
 * @see GuiceyConfigurationRule
 * @see UseGuiceyConfiguration
 * @since 11.04.2018
 */
public final class ConfigurationHooksSupport {
    private static final ThreadLocal<Set<GuiceyConfigurationHook>> HOOKS = new ThreadLocal<>();

    private ConfigurationHooksSupport() {
    }

    /**
     * Register hook for current thread. Must be called before application initialization, otherwise will not
     * be used at all.
     *
     * @param hook hook to register
     */
    public static void register(final GuiceyConfigurationHook hook) {
        if (HOOKS.get() == null) {
            // to avoid duplicate registrations
            HOOKS.set(new LinkedHashSet<>());
        }
        HOOKS.get().add(hook);
    }

    /**
     * May be called to remove improperly registered hooks (registered after context start).
     */
    public static void reset() {
        HOOKS.remove();
    }

    /**
     * Called just after manual application configuration (in application class).
     * Could be used in tests to disable configuration items and (probably) replace them.
     *
     * @param builder just created builder
     * @return used hooks
     */
    public static Set<GuiceyConfigurationHook> run(final GuiceBundle.Builder builder) {
        final Set<GuiceyConfigurationHook> hooks = HOOKS.get();
        if (hooks != null) {
            hooks.forEach(l -> l.configure(builder));
        }
        // clear hooks just after init
        reset();
        return hooks;
    }

    /**
     * @return count of hooks registered in current thread
     */
    public static int count() {
        return HOOKS.get() != null ? HOOKS.get().size() : 0;
    }
}
