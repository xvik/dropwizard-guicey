package ru.vyarus.dropwizard.guice.module.context.debug.report.guice;

import com.google.inject.Module;
import ru.vyarus.dropwizard.guice.module.GuiceBootstrapModule;
import ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Configuration for guice bindings report ({@link GuiceBindingsRenderer}).
 *
 * @author Vyacheslav Rusakov
 * @since 13.08.2019
 */
public class GuiceConfig {

    private List<String> ignorePackages = new ArrayList<>();
    private List<Class<? extends Module>> ignoreModules = new ArrayList<>();

    /**
     * @param pkgs packages to hide bindings and modules from
     * @return config object for chained calls
     */
    public GuiceConfig hidePackages(final String... pkgs) {
        Collections.addAll(ignorePackages, pkgs);
        return this;
    }

    /**
     * @param modules modules to hide
     * @return config object for chained calls
     */
    @SafeVarargs
    public final GuiceConfig hideModules(final Class<? extends Module>... modules) {
        Collections.addAll(ignoreModules, modules);
        return this;
    }

    /**
     * Hide guice bindings.
     *
     * @return config object for chained calls
     */
    public GuiceConfig hideGuiceBindings() {
        return hidePackages("com.google.inject");
    }

    /**
     * Hide guicey bindings. Includes yaml configuration bindings ({@link #hideYamlBindings()}).
     *
     * @return config object for chained calls
     */
    public GuiceConfig hideGuiceyBindings() {
        return hideModules(GuiceBootstrapModule.class);
    }

    /**
     * Hide yaml configuration bindings.
     *
     * @return config object for chained calls
     */
    public GuiceConfig hideYamlBindings() {
        return hideModules(ConfigBindingModule.class);
    }

    /**
     * @return list of packages to ignore or empty list
     */
    public List<String> getIgnorePackages() {
        return ignorePackages;
    }

    /**
     * @return list of modules to ignore or empty list
     */
    public List<Class<? extends Module>> getIgnoreModules() {
        return ignoreModules;
    }
}
