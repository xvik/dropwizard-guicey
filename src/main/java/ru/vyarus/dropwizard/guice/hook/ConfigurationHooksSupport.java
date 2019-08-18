package ru.vyarus.dropwizard.guice.hook;

import com.google.common.base.Preconditions;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.module.installer.util.PropertyUtils;
import ru.vyarus.dropwizard.guice.test.GuiceyHooksRule;
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyHooks;

import java.util.*;

/**
 * Extra configuration mechanism to apply external configuration to bundle builder after manual configuration in
 * application class.
 * <p>
 * Supposed to be used for integration tests.
 *
 * @author Vyacheslav Rusakov
 * @see GuiceyHooksRule
 * @see UseGuiceyHooks
 * @since 11.04.2018
 */
public final class ConfigurationHooksSupport {
    /**
     * Guiey hooks list system property.
     */
    public static final String HOOKS_PROPERTY = "guicey.hooks";

    private static final ThreadLocal<Set<GuiceyConfigurationHook>> HOOKS = new ThreadLocal<>();
    private static final ThreadLocal<Map<String, String>> ALIASES = new ThreadLocal<>();

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
     * Register alias hor hook, loaded from system property. After registration, alias may be used in system
     * property value as shortcut: -Dguicey.hooks=alias,alias2,com.foo.HookFullName.
     *
     * @param alias hook alias name
     * @param hook  hook class
     */
    public static void registerSystemHookAlias(final String alias, final Class<?> hook) {
        Map<String, String> aliases = ALIASES.get();
        if (aliases == null) {
            aliases = new HashMap<>();
            ALIASES.set(aliases);
        }
        Preconditions.checkState(!aliases.containsKey(alias), "Can't register alias '%s' for hook %s "
                + "because it's already registered for hook %s", alias, hook, aliases.get(alias));
        aliases.put(alias, hook.getName());
    }

    /**
     * Registered aliases may be used instead of full hook class name in property value.
     *
     * @return registered system hook aliases or empty map
     */
    public static Map<String, String> getSystemHookAliases() {
        return ALIASES.get() != null ? ALIASES.get() : Collections.emptyMap();
    }

    /**
     * Load hooks from "guicey.hooks" system property.
     */
    public static void loadSystemHooks() {
        final List<GuiceyConfigurationHook> hooks = PropertyUtils.getProperty(HOOKS_PROPERTY, getSystemHookAliases());
        hooks.forEach(ConfigurationHooksSupport::register);
    }

    /**
     * May be called to remove improperly registered hooks (registered after context start).
     */
    public static void reset() {
        HOOKS.remove();
        ALIASES.remove();
        System.clearProperty(HOOKS_PROPERTY);
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
