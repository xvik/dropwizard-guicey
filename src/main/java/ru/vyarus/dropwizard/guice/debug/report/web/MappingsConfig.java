package ru.vyarus.dropwizard.guice.debug.report.web;

/**
 * Web mappings report configuration ({@link WebMappingsRenderer}). By default config will show nothing and
 * user must enable main or admin context (or both). In enabled context only user servlets and filters will be shown,
 * so if you want to see dropwizard bindings - they must be explicitly activated. Guice servlets and filters
 * are also not visible by default.
 *
 * @author Vyacheslav Rusakov
 * @since 24.10.2019
 */
public class MappingsConfig {

    private boolean guiceMappings;
    private boolean dropwizardMappings;
    private boolean mainContext;
    private boolean adminContext;

    /**
     * Show main context mappings.
     *
     * @return config instance for chained calls
     */
    public MappingsConfig showMainContext() {
        mainContext = true;
        return this;
    }

    /**
     * Show admin context mappings.
     *
     * @return config instance for chained calls
     */
    public MappingsConfig showAdminContext() {
        adminContext = true;
        return this;
    }

    /**
     * Show guice {@link com.google.inject.servlet.ServletModule} mappings and
     * {@link com.google.inject.servlet.GuiceFilter} registrations.
     *
     * @return config instance for chained calls
     */
    public MappingsConfig showGuiceMappings() {
        guiceMappings = true;
        return this;
    }

    /**
     * Show default servlets and filters configured by dropwizard (including jersey servlet).
     *
     * @return config instance for chained calls
     */
    public MappingsConfig showDropwizardMappings() {
        dropwizardMappings = true;
        return this;
    }


    /**
     * @return true to show guice filters and servlets together with {@link com.google.inject.servlet.GuiceFilter}
     */
    public boolean isGuiceMappings() {
        return guiceMappings;
    }

    /**
     * @return true to show default servlets and filters
     */
    public boolean isDropwizardMappings() {
        return dropwizardMappings;
    }

    /**
     * @return true to show main context mappings
     */
    public boolean isMainContext() {
        return mainContext;
    }

    /**
     * @return true to show admin context mappings
     */
    public boolean isAdminContext() {
        return adminContext;
    }
}
