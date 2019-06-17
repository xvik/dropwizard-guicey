package ru.vyarus.dropwizard.guice.module.yaml.bind;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.util.Providers;
import io.dropwizard.Configuration;
import ru.vyarus.dropwizard.guice.module.yaml.ConfigurationTree;
import ru.vyarus.dropwizard.guice.module.yaml.ConfigPath;

/**
 * Binds configuration constants. Bindings are qualified with {@link Config}.
 * <p>
 * Note that not all configuration paths may be available because configuration is introspected using jersey
 * serialization api and some configuration classes may only consume properties (e.g. value consumed directly in
 * setter - impossible to read back).
 * <p>
 * All content types are bound by declaration type (as they declared in configuration class). All primitive types
 * are boxed. All collection types like List, Set, Map, Multimap are bound by collection type (List, Set etc).
 * Generics are always used in binding (so {@code @inject @Config("path") List<String> val} will work and
 * {@code @inject @Config("path") List val} will not).
 * <p>
 * Root configuration objects are bound with and without qualifier, except root interfaces which are bound
 * with qualifier only.
 * <p>
 * {@link ConfigurationTree} instance is also bound directly to be used for custom configuration analysis.
 *
 * @author Vyacheslav Rusakov
 * @since 04.05.2018
 * @see Config for more info on usage
 * @see ru.vyarus.dropwizard.guice.GuiceyOptions#BindConfigurationByPath
 */
public class ConfigBindingModule extends AbstractModule {

    private final Configuration configuration;
    private final ConfigurationTree tree;

    public ConfigBindingModule(final Configuration configuration, final ConfigurationTree tree) {
        this.configuration = configuration;
        this.tree = tree;
    }

    @Override
    protected void configure() {
        bind(ConfigurationTree.class).toInstance(tree);

        bindRootTypes();
        bindUniqueSubConfigurations();
        bindValuePaths();
    }


    /**
     * Bind configuration hierarchy: all superclasses and direct interfaces for each level (except common interfaces).
     * Interfaces are bound only with qualifier, except when deprecated option enabled.
     */
    @SuppressWarnings("unchecked")
    private void bindRootTypes() {
        for (Class type : tree.getRootTypes()) {
            // bind root configuration classes both with and without qualifier
            if (!type.isInterface()) {
                // bind interface as type only when it's allowed
                bind(type).toInstance(configuration);
            }
            bind(type).annotatedWith(Config.class).toInstance(configuration);
        }
    }

    /**
     * Bind unique sub configuration objects by type. Available for injection like
     * {@code @Inject @Config MySubConf config}. Value may be null because if null values would be avoided,
     * bindings will disappear.
     */
    @SuppressWarnings("unchecked")
    private void bindUniqueSubConfigurations() {
        for (ConfigPath item : tree.getUniqueTypePaths()) {
            // bind only with annotation to avoid clashes with direct bindings
            toValue(
                    bind(Key.get(item.getDeclaredTypeWithGenerics(), Config.class)),
                    item.getValue());
        }
    }

    /**
     * Bind configuration paths. Available for injection like {@code @Inject @Code("path.sub") Integer conf}.
     * Value may be null because if null values would be avoided, bindings will disappear.
     */
    @SuppressWarnings({"unchecked", "PMD.AvoidInstantiatingObjectsInLoops"})
    private void bindValuePaths() {
        for (ConfigPath item : tree.getPaths()) {
            toValue(
                    bind(Key.get(item.getDeclaredTypeWithGenerics(), new ConfigImpl(item.getPath()))),
                    item.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private void toValue(final LinkedBindingBuilder binding, final Object value) {
        if (value != null) {
            binding.toInstance(value);
        } else {
            binding.toProvider(Providers.of(null));
        }
    }
}
