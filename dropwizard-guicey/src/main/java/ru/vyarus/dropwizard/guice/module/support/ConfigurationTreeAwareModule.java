package ru.vyarus.dropwizard.guice.module.support;

import ru.vyarus.dropwizard.guice.module.yaml.ConfigurationTree;

/**
 * Guice module, registered in bundle may implement this to be able to use introspected configuration in module
 * configuration method.
 *
 * @author Vyacheslav Rusakov
 * @see DropwizardAwareModule
 * @since 11.06.2018
 */
public interface ConfigurationTreeAwareModule {

    /**
     * Mathod will be called just before injector initialization.
     *
     * @param configurationTree introspected configuration object
     */
    void setConfigurationTree(ConfigurationTree configurationTree);
}
