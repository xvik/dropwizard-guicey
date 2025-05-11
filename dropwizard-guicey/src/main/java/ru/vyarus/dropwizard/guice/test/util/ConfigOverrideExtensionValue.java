package ru.vyarus.dropwizard.guice.test.util;

import com.google.common.base.Preconditions;
import io.dropwizard.testing.ConfigOverride;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Config override implementation for values computed in 3rd party junit 5 extensions. Such extension must put
 * value into junit storage (preferably, with key equal to overriding property path). This value would be resolved
 * under junit "before all" phase, just before dropwizard test support object creation (and so 3rd party extension
 * MUST be executed before guicey extension).
 *
 * @author Vyacheslav Rusakov
 * @since 10.05.2022
 */
public class ConfigOverrideExtensionValue extends ConfigOverride implements ConfigurablePrefix {

    private static final String DOT = ".";

    private final Logger logger = LoggerFactory.getLogger(ConfigOverrideExtensionValue.class);

    private final ExtensionContext.Namespace namespace;
    private final String storageKey;
    private final String configPath;
    private String prefix;
    private String originalValue;
    private String value;

    /**
     * Create a config override value.
     *
     * @param namespace  namespace
     * @param storageKey storage key
     * @param configPath config yaml path
     */
    public ConfigOverrideExtensionValue(final ExtensionContext.Namespace namespace,
                                        final String storageKey,
                                        final String configPath) {
        this.namespace = Preconditions.checkNotNull(namespace, "Extension namespace required");
        this.storageKey = Preconditions.checkNotNull(storageKey, "Storage key required");
        this.configPath = Preconditions.checkNotNull(configPath, "Configuration path required");
    }

    @Override
    public void setPrefix(final String prefix) {
        this.prefix = prefix.endsWith(DOT) ? prefix : prefix + DOT;
    }

    /**
     * Called to resolve actual value from configured namespace. 3rd party extension must already initialize
     * value in the store.
     *
     * @param context test context
     */
    public void resolveValue(final ExtensionContext context) {
        final Object res = context.getStore(namespace).get(storageKey);
        if (res != null) {
            value = String.valueOf(res);
        } else {
            logger.warn("Configuration override value for '" + configPath + "' was not initialized by junit "
                    + "extension (under '" + storageKey + "' key). Make sure extensions order is correct.");
            value = "";
        }
    }

    @Override
    public void addToSystemProperties() {
        Preconditions.checkNotNull(prefix, "Prefix is not defined");
        Preconditions.checkNotNull(value, "Value wasn't resolved");
        this.originalValue = System.setProperty(prefix + configPath, value);
    }

    @Override
    public void removeFromSystemProperties() {
        if (originalValue != null) {
            System.setProperty(prefix + configPath, originalValue);
        } else {
            System.clearProperty(prefix + configPath);
        }
    }
}
