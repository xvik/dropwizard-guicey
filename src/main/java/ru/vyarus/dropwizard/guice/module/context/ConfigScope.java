package ru.vyarus.dropwizard.guice.module.context;

import io.dropwizard.Bundle;
import ru.vyarus.dropwizard.guice.bundle.GuiceyBundleLookup;
import ru.vyarus.dropwizard.guice.module.installer.scanner.ClasspathScanner;
import ru.vyarus.dropwizard.guice.module.support.conf.GuiceyConfigurator;

import java.util.Arrays;

/**
 * Enum with type constants used for marking special configuration scopes. Guicey bundles are also
 * scopes: all items registered by guicey bunlde is scoped with bundle type.
 * <p>
 * Scope represents configuration source (configuration entry point).
 *
 * @author Vyacheslav Rusakov
 * @since 16.04.2018
 */
public enum ConfigScope {
    /**
     * Application scope: everything registered directly in guice bundle's builder.
     */
    Application(io.dropwizard.Application.class),
    /**
     * Lookup scope contains all bundles, resolved with lookup mechanism.
     */
    BundleLookup(GuiceyBundleLookup.class),
    /**
     * Guicey bundles resolved from (manually) registered dropwizard bundles
     * (not enabled by default).
     */
    DropwizardBundle(Bundle.class),
    /**
     * Everything resolved with classpath scan.
     */
    ClasspathScan(ClasspathScanner.class),
    /**
     * All configurations done by {@link GuiceyConfigurator} (most likely in integration tests).
     */
    Configurator(GuiceyConfigurator.class);

    /* For guicey bundles scope type will equal to bundle class */


    private final Class<?> type;

    ConfigScope(final Class<?> type) {
        this.type = type;
    }

    /**
     * @return class used for special scope identification
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * Useful for hiding all special scopes except one in diagnostic report.
     *
     * @param scope scope to exclude
     * @return all special scopes except provided
     */
    public static Class[] allExcept(final ConfigScope scope) {
        return Arrays.stream(values())
                .filter(s -> s != scope)
                .map(ConfigScope::getType)
                .toArray(Class[]::new);
    }
}
