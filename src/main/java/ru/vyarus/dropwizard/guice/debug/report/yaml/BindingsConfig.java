package ru.vyarus.dropwizard.guice.debug.report.yaml;

/**
 * Configuration for configuration bindings report ({@link ConfigBindingsRenderer}).
 *
 * @author Vyacheslav Rusakov
 * @since 13.06.2018
 */
public class BindingsConfig {

    private boolean configurationTree;
    private boolean bindings = true;
    private boolean nullValues;
    private boolean onlyCustomConfigs;

    /**
     * Show visible configuration tree. Note that other options affects tree render.
     *
     * @return config object for chained calls
     */
    public BindingsConfig showConfigurationTree() {
        configurationTree = true;
        return this;
    }

    /**
     * Show configuration values tree only without bindings.
     *
     * @return config object for chained calls
     */
    public BindingsConfig showConfigurationTreeOnly() {
        showConfigurationTree();
        bindings = false;
        return this;
    }

    /**
     * Show paths with null values.
     *
     * @return config object for chained calls
     */
    public BindingsConfig showNullValues() {
        nullValues = true;
        return this;
    }

    /**
     * Avoid paths from dropwizard {@link io.dropwizard.core.Configuration} class (only custom configuration paths
     * shown).
     *
     * @return config object for chained calls
     */
    public BindingsConfig showCustomConfigOnly() {
        onlyCustomConfigs = true;
        return this;
    }

    /**
     * @return true to print configuration tree, false to avoid
     */
    public boolean isShowConfigurationTree() {
        return configurationTree;
    }

    /**
     * @return true to print bindings, false to avoid (assumed tree is enabled to show only tree)
     */
    public boolean isShowBindings() {
        return bindings;
    }

    /**
     * @return true to show paths with null values, false to hide null value paths
     */
    public boolean isShowNullValues() {
        return nullValues;
    }

    /**
     * @return true to hide dropwizard configuration bindings, false to show all binding paths
     */
    public boolean isShowCustomConfigOnly() {
        return onlyCustomConfigs;
    }
}
