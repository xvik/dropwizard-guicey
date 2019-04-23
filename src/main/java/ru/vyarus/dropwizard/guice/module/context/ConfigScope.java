package ru.vyarus.dropwizard.guice.module.context;

import io.dropwizard.ConfiguredBundle;
import ru.vyarus.dropwizard.guice.bundle.GuiceyBundleLookup;
import ru.vyarus.dropwizard.guice.module.installer.scanner.ClasspathScanner;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;

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
    DropwizardBundle(ConfiguredBundle.class),
    /**
     * Everything resolved with classpath scan.
     */
    ClasspathScan(ClasspathScanner.class),
    /**
     * All configurations done by {@link GuiceyConfigurationHook} (most likely in integration tests).
     */
    Hook(GuiceyConfigurationHook.class),
    /**
     * WARNING: guicey bundle scope is bundle class itself. Constant is useless for direct usage!
     * It was added just for completeness of context recognition logic (see {@link #recognize(Class)})
     * and to indicate all possible scopes.
     */
    GuiceyBundle(ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle.class);


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
                .filter(s -> s != scope && s != ConfigScope.GuiceyBundle)
                .map(ConfigScope::getType)
                .toArray(Class[]::new);
    }

    /**
     * Scope recognition logic may be used in configration analyzers to easily detect item scope.
     *
     * @param type type to recognize scope
     * @return recognized scope
     * @throws IllegalStateException is scope is not recognizable
     */
    public static ConfigScope recognize(final Class<?> type) {
        for (ConfigScope scope : values()) {
            if (scope.getType().isAssignableFrom(type)) {
                return scope;
            }
        }
        throw new IllegalStateException("Type " + type.getName() + " does not represent configuration scope");
    }
}
