package ru.vyarus.dropwizard.guice.module.context.option.mapper;

import org.apache.commons.lang3.StringUtils;
import ru.vyarus.dropwizard.guice.module.context.option.Option;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Helper utility class for mapping options from system properties, environment variables or direct string.
 * Used with
 * {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder#options(Map)}. May recognize multiple prefixed options from
 * properties (see {@link #props(String)}).
 * <p>
 * Only existing properties are mapped. If value is null or property is not defined - it's ignored.
 * <p>
 * Example usage:
 * <code><pre>
 *     builder.options(new OptionsMapper()
 *                              .env("ENV_PROP", MyOptions.Opt1)
 *                              .env("ENV_PROP_CUST", MyOptions.Opt2, val -> convertVal(val))
 *                              .prop("sys.prop", MyOptions.Opt3)
 *                              .string(MyOptions.Opt4, "value")
 *                              .map())
 *
 * </pre></code>
 * <p>
 * Only string, boolean, integer, double, short. byte enum by value, enum by class, array and EnumSet option types are
 * directly supported (see {@link StringConverter}). For other types explicit converters must be used.
 * <p>
 * To enable generic options definition from properties use:
 * <code><pre>
 *     .options(new OptionsMapper().props().map())
 * </pre></code>
 * This will map system properties prefixed with "option." and with full enum class and value. For example,
 * {@code -Doption.ru.vyarus.dropwizard.guice.GuiceyOptions.InjectorStage=DEVELOPMENT}.
 * <p>
 * In order to apply special conversion for some property (when default converters can't properly convert value)
 * declare manual property binding before mass processing:
 * <code><pre>
 * .options(new OptionsMapper()
 *            .prop("option.ru.vyarus.dropwizard.guice.GuiceyOptions.InjectorStage", GuiceyOptions.InjectorStage,
 *                 val -> someConversion(val))
 *            .props()
 *            .map())
 * </pre></code>
 * And option will be ignored during mass processing (because it's processed manually).
 * <p>
 * To see actual assignments (for debug) use {@link #printMappings()}.
 *
 * @author Vyacheslav Rusakov
 * @since 26.04.2018
 */
public class OptionsMapper {

    private static final String PROP_PREFIX = "prop: ";
    
    private final Map<Enum, Object> options = new HashMap<>();
    private final Set<String> mappedProps = new HashSet<>();
    private boolean print;


    /**
     * Enable assignment logging (to system out, as loggers are not yet initialized).
     *
     * @return mapper instance for chained calls
     */
    public OptionsMapper printMappings() {
        print = true;
        return this;
    }

    /**
     * Shortcut for {@link #props(String)} with "option." prefix.
     *
     * @return mapper instance for chained calls
     */
    public OptionsMapper props() {
        return props("option.");
    }

    /**
     * Search for system properties, prefixed with provided string. Property format must be:
     * "(prefix)enumClassName.enumValue". For example: option.ru.vyarus.dropwizard.guice.GuiceyOptions.UseHkBridge
     * where "option." is prefix.
     * <p>
     * Could be used only for string, boolean, integer, double, short and byte option types.
     * <p>
     * If some options require special handling then map them directly using {@link #prop(String, Enum, Function)}
     * and only AFTER that call mass processing.
     *
     * @return mapper instance for chained calls
     */
    public <T extends Enum & Option> OptionsMapper props(final String prefix) {
        for (Object key : System.getProperties().keySet()) {
            final String name = (String) key;
            // don't look for directly mapped properties
            if (!mappedProps.contains(name) && name.startsWith(prefix)) {
                final String optionName = name.substring(prefix.length());
                final T option = OptionParser.recognizeOption(optionName);
                register(PROP_PREFIX + name, option, System.getProperty(name), null);
            }
        }
        return this;
    }

    /**
     * Shortcut version of {@link #prop(String, Enum, Function)}. Used when default converters could be used.
     *
     * @param name   property name
     * @param option option
     * @param <T>    helper option type
     * @return mapper instance for chained calls
     */
    public <T extends Enum & Option> OptionsMapper prop(final String name, final T option) {
        return prop(name, option, null);
    }

    /**
     * Directly map system property value to option.
     * <p>
     * Directly mapped properties excluded from mass processing in {@link #props(String)}, but direct mapping
     * must appear before mass-processing.
     *
     * @param name      property name
     * @param option    option
     * @param converter value converter (may be null to use default converters)
     * @param <T>       helper option type
     * @return mapper instance for chained calls
     */
    public <V, T extends Enum & Option> OptionsMapper prop(final String name, final T option,
                                                           final Function<String, V> converter) {
        mappedProps.add(name);
        register(PROP_PREFIX + name, option, System.getProperty(name), converter);
        return this;
    }

    /**
     * Shortcut version of {@link #env(String, Enum, Function)}. Used when default converters could be used.
     *
     * @param name   environment variable name
     * @param option option
     * @param <T>    helper option type
     * @return mapper instance for chained calls
     */
    public <T extends Enum & Option> OptionsMapper env(final String name, final T option) {
        return env(name, option, null);
    }

    /**
     * Map environment variable value to option.
     *
     * @param name      environment variable name
     * @param option    option
     * @param converter value converter (may be null to use default converters)
     * @param <V>       helper value type
     * @param <T>       helper option type
     * @return mapper instance for chained calls
     */
    public <V, T extends Enum & Option> OptionsMapper env(final String name, final T option,
                                                          final Function<String, V> converter) {
        register("env: " + name, option, System.getenv(name), converter);
        return this;
    }

    /**
     * Shortcut version of {@link #string(Enum, String, Function)}. Used when default converters could be used.
     *
     * @param option option
     * @param value  value string (could be null)
     * @param <V>    helper value type
     * @param <T>    helper option type
     * @return mapper instance for chained calls
     */
    public <V, T extends Enum & Option> OptionsMapper string(final T option, final String value) {
        return string(option, value, null);
    }

    /**
     * Map string to option. When value is null or empty - nothing happens.
     *
     * @param option    option
     * @param value     value string (could be null)
     * @param converter value converter (may be null to use default converters)
     * @param <V>       helper value type
     * @param <T>       helper option type
     * @return mapper instance for chained calls
     */
    public <V, T extends Enum & Option> OptionsMapper string(final T option, final String value,
                                                             final Function<String, V> converter) {
        register("", option, value, converter);
        return this;
    }

    /**
     * @return map of resolved options
     */
    public Map<Enum, Object> map() {
        return options;
    }

    @SuppressWarnings("PMD.SystemPrintln")
    private <T extends Enum & Option> void register(final String source, final T option, final String value,
                                                    final Function<String, ?> converter) {
        if (StringUtils.isNotBlank(value)) {
            options.put(option, converter == null ? OptionParser.parseValue(option, value)
                    : converter.apply(value));
            if (print) {
                System.out.printf("\t%-25s  %s.%s = %s",
                        source, option.getDeclaringClass().getSimpleName(), option.name(), value).println();
            }
        }
    }
}
