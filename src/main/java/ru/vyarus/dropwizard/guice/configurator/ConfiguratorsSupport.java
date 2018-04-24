package ru.vyarus.dropwizard.guice.configurator;

import ru.vyarus.dropwizard.guice.GuiceBundle;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Extra configuration mechanism applies external configuration to bundle builder after manual configuration in
 * application class.
 * <p>
 * Supposed to be used for integration tests.
 *
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.dropwizard.guice.test.GuiceyConfiguratorRule
 * @see ru.vyarus.dropwizard.guice.test.spock.UseGuiceyConfigurator
 * @since 11.04.2018
 */
public final class ConfiguratorsSupport {
    private static final ThreadLocal<Set<GuiceyConfigurator>> CONFIGURATORS = new ThreadLocal<>();

    private ConfiguratorsSupport() {
    }

    /**
     * Register configurator for current thread. Must be called before application initialization, otherwise will not
     * be used at all.
     *
     * @param configurator configurator to register
     */
    public static void listen(final GuiceyConfigurator configurator) {
        if (CONFIGURATORS.get() == null) {
            // to avoid duplicate registrations
            CONFIGURATORS.set(new LinkedHashSet<>());
        }
        CONFIGURATORS.get().add(configurator);
    }

    /**
     * May be called to remove improperly registered configurators (registered after context start).
     */
    public static void reset() {
        CONFIGURATORS.remove();
    }

    /**
     * Called just after manual application configuration (in application class).
     * Could be used in tests to disable configuration items and (probably) replace them.
     *
     * @param builder just created builder
     * @return used configurators
     */
    public static Set<GuiceyConfigurator> configure(final GuiceBundle.Builder builder) {
        final Set<GuiceyConfigurator> confs = CONFIGURATORS.get();
        if (confs != null) {
            confs.forEach(l -> l.configure(builder));
        }
        // clear configurators just after init
        reset();
        return confs;
    }

    /**
     * @return count of configurators registered in current thread
     */
    public static int count() {
        return CONFIGURATORS.get() != null ? CONFIGURATORS.get().size() : 0;
    }
}
