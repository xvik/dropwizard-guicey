package ru.vyarus.dropwizard.guice.module.context.option.mapper;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import ru.vyarus.dropwizard.guice.module.context.option.Option;

/**
 * Utility class for options recognition from string and value parsing.
 *
 * @author Vyacheslav Rusakov
 * @see OptionsMapper
 * @since 26.04.2018
 */
public final class OptionParser {

    private OptionParser() {
    }

    /**
     * Recognize option from string. Format: "optionType.optionName".
     *
     * @param option option name
     * @param <T>    target option type
     * @return recognized option enum
     */
    @SuppressWarnings("unchecked")
    public static <T extends Enum & Option> T recognizeOption(final String option) {
        final Enum res = StringConverter.convert(Enum.class, option);
        Preconditions.checkState(Option.class.isAssignableFrom(res.getDeclaringClass()),
                "%s is not an option type (must be enum and implement Option interface)",
                res.getDeclaringClass());
        return (T) res;
    }

    /**
     * Parse option value from string. Only basic conversions are supported: like string, boolean, integer, double,
     * enum value, enum by class and arrays of these types (see {@link StringConverter}).
     *
     * @param option option enum
     * @param value  string value
     * @param <V>    option value type
     * @param <T>    option type
     * @return parsed option value
     */
    @SuppressWarnings("unchecked")
    public static <V, T extends Enum & Option> V parseValue(final T option, final String value) {
        Preconditions.checkState(StringUtils.isNotBlank(value),
                "Empty value is not allowed for option %s", option);
        try {
            return (V) StringConverter.convert(option.getType(), value);
        } catch (Exception ex) {
            throw new IllegalStateException(String.format("Failed to convert '%s' value for option %s.%s",
                    value, option.getDeclaringClass().getSimpleName(), option), ex);
        }
    }
}
