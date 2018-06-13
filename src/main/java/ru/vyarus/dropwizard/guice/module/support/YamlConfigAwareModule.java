package ru.vyarus.dropwizard.guice.module.support;

import ru.vyarus.dropwizard.guice.module.yaml.YamlConfig;

/**
 * Guice module, registered in bundle may implement this to be able to use introspected configuration in module
 * configuration method.
 *
 * @author Vyacheslav Rusakov
 * @see DropwizardAwareModule
 * @since 11.06.2018
 */
public interface YamlConfigAwareModule {

    /**
     * Mathod will be called just before injector initialization.
     *
     * @param yamlConfig introspected configuration object
     */
    void setYamlConfig(YamlConfig yamlConfig);
}
