package ru.vyarus.dropwizard.guice.debug.report.option;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Options reporting configuration. By default, only user defined options are printed without not used options
 * indication. Configuration could be performed in chained fashion:
 * <pre><code>
 *     OptionsConfig config = new OptionsConfig()
 *          .showNotUsedMarker()
 *          .showNotDefinedOptions()
 * </code></pre>
 *
 * @author Vyacheslav Rusakov
 * @see OptionsRenderer for usage
 * @since 22.08.2016
 */
public class OptionsConfig {

    private final Set<Class<Enum>> hiddenGroups = new HashSet<>();
    private final Set<Enum> hidden = new HashSet<>();
    private boolean notUsed;
    private boolean notDefined;

    /**
     * @return true to show not used options marker, false otherwise
     */
    public boolean isShowNotUsedMarker() {
        return notUsed;
    }

    /**
     * CUSTOM marker is shown only when option enabled.
     *
     * @return true to show options with default values (not overridden by user), false otherwise
     */
    public boolean isShowNotDefinedOptions() {
        return notDefined;
    }

    /**
     * @return set of option classed to hide or empty set
     */
    public Set<Class<Enum>> getHiddenGroups() {
        return hiddenGroups;
    }

    /**
     * @return set of options to hide or empty set
     */
    public Set<Enum> getHiddenOptions() {
        return hidden;
    }

    /**
     * Show NOT_USED marker for not (yet) used options.
     *
     * @return config instance for chained calls
     */
    public OptionsConfig showNotUsedMarker() {
        notUsed = true;
        return this;
    }

    /**
     * Show options not customized by user (in other words show used options defaults).
     * When enabled, CUSTOM marker is shown for user defined options.
     *
     * @return config instance for chained calls
     */
    public OptionsConfig showNotDefinedOptions() {
        notDefined = true;
        return this;
    }

    /**
     * Hide option groups from reporting.
     *
     * @param groups option enum classes to hide
     * @return config instance for chained calls
     */
    @SafeVarargs
    public final OptionsConfig hideGroups(final Class<Enum>... groups) {
        hiddenGroups.addAll(Arrays.asList(groups));
        return this;
    }

    /**
     * Hide exact options from reporting.
     *
     * @param options options to hide
     * @return config instance for chained calls
     */
    public OptionsConfig hideOptions(final Enum... options) {
        hidden.addAll(Arrays.asList(options));
        return this;
    }
}
