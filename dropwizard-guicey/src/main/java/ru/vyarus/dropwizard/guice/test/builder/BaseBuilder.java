package ru.vyarus.dropwizard.guice.test.builder;

import com.google.common.base.Preconditions;
import io.dropwizard.configuration.ConfigurationSourceProvider;
import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.testing.ConfigOverride;
import jakarta.annotation.Nullable;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.test.util.ConfigModifier;
import ru.vyarus.dropwizard.guice.test.util.ConfigOverrideUtils;
import ru.vyarus.dropwizard.guice.test.util.HooksUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Base class for test support objects builders.
 *
 * @param <C> configuration type
 * @param <T> builder type
 * @author Vyacheslav Rusakov
 * @since 20.11.2023
 */
@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public abstract class BaseBuilder<C extends Configuration, T extends BaseBuilder<C, T>> {
    /**
     * Application class.
     */
    protected final Class<? extends Application<C>> app;
    /**
     * Configuration file path.
     */
    protected String configPath;
    /**
     * Configuration source provider.
     */
    protected ConfigurationSourceProvider configSourceProvider;
    /**
     * Configuration overrides.
     */
    protected final Map<String, Supplier<String>> configOverrides = new HashMap<>();
    /**
     * Configuration modifiers.
     */
    protected final List<ConfigModifier<C>> modifiers = new ArrayList<>();
    /**
     * Configuration instance (instead of file).
     */
    protected C configObject;
    /**
     * Configuration overrides property prefix.
     */
    protected String propertyPrefix;
    /**
     * Rest context mapping.
     */
    protected String restMapping;

    /**
     * Create builder.
     *
     * @param app application class
     */
    public BaseBuilder(final Class<? extends Application<C>> app) {
        this.app = app;
    }

    /**
     * Must not be used if {@link #config(io.dropwizard.core.Configuration)} used.
     *
     * @param path configuration file path
     * @return builder instance for chained calls
     */
    public T config(final @Nullable String path) {
        this.configPath = path;
        return self();
    }

    /**
     * Use configuration instance instead of configuration parsing from yaml file. When this is used, other
     * configuration options must not be used (they can't be used, and an error would be thrown indicating incorrect
     * usage).
     *
     * @param config pre-initialized configuration object
     * @return builder instance for chained calls
     */
    public T config(final @Nullable C config) {
        this.configObject = config;
        return self();
    }

    /**
     * Must not be used if {@link #config(io.dropwizard.core.Configuration)} used.
     *
     * @param provider configuration source provider
     * @return builder instance for chained calls
     */
    public T configSourceProvider(final @Nullable ConfigurationSourceProvider provider) {
        this.configSourceProvider = provider;
        return self();
    }

    /**
     * Must not be used if {@link #config(io.dropwizard.core.Configuration)} used.
     *
     * @param overrides config override values (in format "path: value")
     * @return builder instance for chained calls
     * @see #configModifiers(ru.vyarus.dropwizard.guice.test.util.ConfigModifier[])
     */
    public T configOverrides(final String... overrides) {
        for (String over : overrides) {
            configOverride(over);
        }
        return self();
    }

    /**
     * Must not be used if {@link #config(io.dropwizard.core.Configuration)} used.
     *
     * @param override config override value (in format "path: value")
     * @return builder instance for chained calls
     * @see #configModifiers(ru.vyarus.dropwizard.guice.test.util.ConfigModifier[])
     */
    public T configOverride(final @Nullable String override) {
        if (override != null) {
            final int idx = override.indexOf(':');
            Preconditions.checkState(idx > 0,
                    "Incorrect configuration override declaration: must be 'key: value', but found '%s'", override);
            configOverride(override.substring(0, idx).trim(), override.substring(idx + 1).trim());
        }
        return self();
    }

    /**
     * Must not be used if {@link #config(io.dropwizard.core.Configuration)} used.
     *
     * @param key   configuration path
     * @param value overriding value
     * @return builder instance for chained calls
     * @see #configModifiers(ru.vyarus.dropwizard.guice.test.util.ConfigModifier[])
     */
    public T configOverride(final String key, final String value) {
        return configOverride(key, () -> value);
    }

    /**
     * Must not be used if {@link #config(io.dropwizard.core.Configuration)} used.
     *
     * @param key   configuration path
     * @param value overriding value provider
     * @return builder instance for chained calls
     */
    public T configOverride(final String key, final Supplier<String> value) {
        this.configOverrides.put(key, value);
        return self();
    }

    /**
     * Configuration modifier is an alternative for configuration override, which is limited for simple
     * property types (for example, a collection could not be overridden).
     * <p>
     * Modifier is called before application run phase. Only logger configuration is applied at this moment (and so you
     * can't change it). Modifier would work with both yaml ({@link #config(String)}) and instance
     * ({@link #config(io.dropwizard.core.Configuration)}) based configurations.
     * <p>
     * Method supposed to be used with lambdas and so limited for application configuration class.
     * For generic configurations (based on configuration subclass or raw {@link io.dropwizard.core.Configuration})
     * use {@link #configModifiers(Class[])}.
     *
     * @param modifiers configuration modifiers
     * @return builder instance for chained calls
     */
    @SafeVarargs
    public final T configModifiers(final ConfigModifier<C>... modifiers) {
        Collections.addAll(this.modifiers, modifiers);
        return self();
    }

    /**
     * Configuration modifier is an alternative for configuration override, which is limited for simple
     * property types (for example, a collection could not be overridden).
     * <p>
     * Modifier is called before application run phase. Only logger configuration is applied at this moment (and so you
     * can't change it). Modifier would work with both yaml ({@link #config(String)}) and instance
     * ({@link #config(io.dropwizard.core.Configuration)}) based configurations.
     * <p>
     * Method is useful for generic modifiers (based on configuration subclass or raw
     * {@link io.dropwizard.core.Configuration}).
     *
     * @param modifiers configuration modifiers
     * @return builder instance for chained calls
     */
    @SafeVarargs
    public final T configModifiers(final Class<? extends ConfigModifier<? extends Configuration>>... modifiers) {
        this.modifiers.addAll(ConfigOverrideUtils.createModifiers(modifiers));
        return self();
    }

    /**
     * Dropwizard stored all provided configuration overriding values as system properties with provided prefix
     * (or "dw." by default). If multiple tests run concurrently, they would collide on using the same system
     * properties. It is preferred to specify test-unique prefix.
     *
     * @param prefix configuration override properties prefix
     * @return builder instance for chained calls
     */
    public T propertyPrefix(final @Nullable String prefix) {
        this.propertyPrefix = prefix;
        return self();
    }


    /**
     * Shortcut for hooks registration (method simply immediately registers provided hooks).
     *
     * @param hooks hook classes to install (nulls not allowed)
     * @return builder instance for chained calls
     */
    @SafeVarargs
    public final T hooks(final Class<? extends GuiceyConfigurationHook>... hooks) {
        HooksUtil.register(HooksUtil.create(hooks));
        return self();
    }

    /**
     * Shortcut for hooks registration (method simply immediately registers provided hooks).
     *
     * @param hooks hooks to install (nulls allowed)
     * @return builder instance for chained calls
     */
    public T hooks(final GuiceyConfigurationHook... hooks) {
        HooksUtil.register(Arrays.asList(hooks));
        return self();
    }

    /**
     * Collect configuration overrides objects.
     *
     * @param prefix custom configuration overrides prefix
     * @return configuration overrides
     */
    protected ConfigOverride[] prepareOverrides(final String prefix) {
        final ConfigOverride[] override = new ConfigOverride[configOverrides.size() + (restMapping == null ? 0 : 1)];
        int i = 0;
        for (Map.Entry<String, Supplier<String>> entry : configOverrides.entrySet()) {
            override[i++] = ConfigOverride.config(prefix,
                    entry.getKey(), entry.getValue());
        }
        if (restMapping != null) {
            override[i] = ConfigOverrideUtils.overrideRestMapping(prefix, restMapping);
        }
        return override;
    }

    /**
     * Specifies rest mapping path. This is the same as specifying direct config override
     * {@code "server.rootMapping: /something/*"}. Specified value would be prefixed with "/" and, if required
     * "/*" applied at the end. So it would be correct to specify {@code restMapping = "api"} (actually set value
     * would be "/api/*").
     * <p>
     * This option is only intended to simplify cases when custom configuration file is not yet used in tests
     * (usually early PoC phase). It allows you to map servlet into application root in test (because rest is no
     * more resided in root). When used with existing configuration file, this parameter will override file definition.
     *
     * @param restMapping rest mapping path
     * @return builder instance for chained calls
     */
    public T restMapping(final String restMapping) {
        this.restMapping = restMapping;
        return self();
    }

    @SuppressWarnings("unchecked")
    private T self() {
        return (T) this;
    }
}
