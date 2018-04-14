package ru.vyarus.dropwizard.guice.module.support.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.GuiceBundle;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Pre configuration mechanism applies external configuration to bundle builder BEFORE application
 * configuration. It is important to do it before because of possible use of generic configurator
 * {@link GuiceBundle.Builder#disable(java.util.function.Predicate[])}. But it also means that it would NOT be possible
 * to override option set by application (because application will always override pre-configured value).
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

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfiguratorsSupport.class);

    private ConfiguratorsSupport() {
    }

    /**
     * Register configurator for current thread. Must be called before application initialization, otherwise will throw
     * exception to indicate wrong registration time.
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
     * Called just after bundle builder creation (before actual application configuration).
     * Should be used in tests to disable configuration items and (probably) replace them.
     *
     * @param builder just created builder
     */
    public static void configure(final GuiceBundle.Builder builder) {
        final Set<GuiceyConfigurator> confs = CONFIGURATORS.get();
        if (confs != null) {
            LOGGER.info("Processing {} configurators", confs.size());
            confs.forEach(l -> l.configure(builder));
        }
        // clear configurators just after init
        reset();
    }

    /**
     * @return count of configurators registered in current thread
     */
    public static int count() {
        return CONFIGURATORS.get() != null ? CONFIGURATORS.get().size() : 0;
    }
}
