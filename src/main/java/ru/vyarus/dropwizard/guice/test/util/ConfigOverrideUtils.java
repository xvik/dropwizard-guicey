package ru.vyarus.dropwizard.guice.test.util;

import com.google.common.base.Preconditions;
import io.dropwizard.testing.ConfigOverride;

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
     * @param props overriding properties in "key=value" format
     * @return parsed configuration override objects
     */
    public static ConfigOverride[] convert(final String... props) {
        ConfigOverride[] overrides = null;
        if (props != null && props.length > 0) {
            overrides = new ConfigOverride[props.length];
            int i = 0;
            for (String value : props) {
                final int idx = value.indexOf('=');
                Preconditions.checkState(idx > 0,
                        "Incorrect configuration override declaration: must be 'key=value', but found '%s'", value);
                overrides[i++] = ConfigOverride.config(value.substring(0, idx), value.substring(idx + 1));
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
        if (addition.length == 0) {
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
}
