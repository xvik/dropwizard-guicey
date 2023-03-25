package ru.vyarus.dropwizard.guice.module.context.option.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import ru.vyarus.dropwizard.guice.module.context.option.Option;

import java.util.Map;
import java.util.Set;

/**
 * Options support logic. Holds configured options and controls option correctness.
 * <p>
 * Because enums are used for option definition, option value checks couldn't be performed at compile time.
 * Check is performed manually during value assignment.
 * <p>
 * Option may have null as default value, but null can't be assigned manually.
 *
 * @param <T> helper type to define option
 * @author Vyacheslav Rusakov
 * @since 09.08.2016
 */
@SuppressWarnings("unchecked")
public final class OptionsSupport<T extends Enum & Option> {

    private final Map<T, OptionHolder> options = Maps.newHashMap();

    /**
     * @param option option enum
     * @param value  option value (not null)
     * @throws NullPointerException     for null value
     * @throws IllegalArgumentException for value incompatible with option type
     */
    public void set(final T option, final Object value) {
        Preconditions.checkNotNull(value, "Null value provided for option %s", option);
        Preconditions.checkArgument(option.getType().isAssignableFrom(value.getClass()),
                "Bad value provided for option %s: %s", option, value);

        getOrCreateHolder(option).setValue(value);
    }

    /**
     * @param option option enum
     * @param <P>    option value
     * @return defined or default option value
     */
    public <P> P get(final T option) {
        return (P) getOrCreateHolder(option).getValue();
    }

    /**
     * @param option option
     * @return option holder object
     * @throws IllegalArgumentException if option is not registered (not used)
     */
    public OptionHolder getHolder(final T option) {
        final OptionHolder holder = options.get(option);
        Preconditions.checkArgument(holder != null, "Option %s was not used", option);
        return holder;
    }

    /**
     * @param option option
     * @return true if option registered (was used or set), false otherwise
     */
    public boolean containsOption(final T option) {
        return options.containsKey(option);
    }

    /**
     * @return set with all used options
     */
    public Set<T> getOptions() {
        return Sets.newHashSet(options.keySet());
    }

    private OptionHolder getOrCreateHolder(final T option) {
        OptionHolder holder = options.get(option);
        if (holder == null) {
            holder = new OptionHolder(option);
            options.put(option, holder);
        }
        return holder;
    }
}
