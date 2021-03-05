package ru.vyarus.dropwizard.guice.test.util;

import com.google.common.base.Preconditions;
import io.dropwizard.testing.ConfigOverride;
import ru.vyarus.dropwizard.guice.debug.util.RenderUtils;

import java.util.List;

/**
 * Config override handling utils.
 *
 * @author Vyacheslav Rusakov
 * @since 30.04.2020
 */
public final class ConfigOverrideUtils {

    private ConfigOverrideUtils() {
    }

    /**
     * Unique prefix is important because config overrides works through system properties and without unique prefix
     * it would be impossible to use parallel tests.
     *
     * @param type test class
     * @return unique properties prefix to use for this test
     */
    public static String createPrefix(final Class<?> type) {
        return RenderUtils.getClassName(type);
    }

    /**
     * @param prefix prefix
     * @param props  overriding properties in "key: value" format
     * @return parsed configuration override objects
     */
    public static ConfigOverride[] convert(final String prefix, final String... props) {
        ConfigOverride[] overrides = null;
        if (props != null && props.length > 0) {
            overrides = new ConfigOverride[props.length];
            int i = 0;
            for (String value : props) {
                final int idx = value.indexOf(':');
                Preconditions.checkState(idx > 0 && idx < value.length(),
                        "Incorrect configuration override declaration: must be 'key: value', but found '%s'", value);
                overrides[i++] = ConfigOverride
                        .config(prefix, value.substring(0, idx).trim(), value.substring(idx + 1).trim());
            }
        }
        return overrides;
    }

    /**
     * Adds config override for existing overrides array.
     *
     * @param base     existing overrides (may be null)
     * @param addition additional overrides (may be empty)
     * @return merged overrides
     */
    @SuppressWarnings("checkstyle:ReturnCount")
    public static ConfigOverride[] merge(final ConfigOverride[] base, final ConfigOverride... addition) {
        if (addition == null || addition.length == 0) {
            return base;
        }
        if (base == null) {
            return addition;
        }
        final ConfigOverride[] res = new ConfigOverride[base.length + addition.length];
        System.arraycopy(base, 0, res, 0, base.length);
        System.arraycopy(addition, 0, res, base.length, addition.length);
        return res;
    }

    /**
     * Process provided custom config override objects by setting context prefix.
     *
     * @param prefix test specific prefix
     * @param values objects to process
     * @param <T>    composite helper type
     * @return array of processed objects or null if nothing registered
     */
    public static <T extends ConfigOverride & ConfigurablePrefix> ConfigOverride[] prepareOverrides(
            final String prefix, final List<T> values) {
        ConfigOverride[] res = null;
        if (!values.isEmpty()) {
            res = new ConfigOverride[values.size()];
            int i = 0;
            for (T value : values) {
                value.setPrefix(prefix);
                res[i++] = value;
            }
        }
        return res;
    }
}
