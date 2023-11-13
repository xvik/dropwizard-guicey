package ru.vyarus.dropwizard.guice.module.yaml.bind;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.util.Providers;
import io.dropwizard.Configuration;
import ru.vyarus.dropwizard.guice.module.yaml.ConfigPath;
import ru.vyarus.dropwizard.guice.module.yaml.ConfigurationTree;
import ru.vyarus.java.generics.resolver.context.container.ParameterizedTypeImpl;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

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
 * @see Config for more info on usage
 * @see ru.vyarus.dropwizard.guice.GuiceyOptions#BindConfigurationByPath
 * @since 04.05.2018
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

        bindCustomQualifiers();
        bindRootTypes();
        bindUniqueSubConfigurations();
        bindValuePaths();
    }

    /**
     * Bind configuration properties, annotated with custom qualifiers. If the same "qualifier + type" is detected,
     * all such values are grouped with {@code Set}.
     */
    private void bindCustomQualifiers() {
        final Multimap<Key<?>, ConfigPath> bindings = LinkedHashMultimap.create();
        for (ConfigPath item : tree.getPaths()) {
            if (item.getQualifier() != null) {
                final Key<?> key = Key.get(item.getDeclaredTypeWithGenerics(), item.getQualifier());
                bindings.put(key, item);
            }
        }

        for (Key<?> key : bindings.keySet()) {
            final Collection<ConfigPath> values = bindings.get(key);
            // single value case
            final ConfigPath first = values.iterator().next();
            Object value = first.getValue();

            Key<?> bindingKey = key;
            if (values.size() > 1) {
                // aggregate multiple values into set
                // NOTE no need to check types compatibility because matching was based on pre-computed keys
                value = values.stream().map(ConfigPath::getValue).collect(Collectors.toSet());
                bindingKey = Key.get(new ParameterizedTypeImpl(Set.class, first.getDeclaredTypeWithGenerics()),
                        first.getQualifier());
            }
            bindValue(bind(bindingKey), value);
        }
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
    private void bindUniqueSubConfigurations() {
        for (ConfigPath item : tree.getUniqueTypePaths()) {
            // bind only with annotation to avoid clashes with direct bindings
            bindValue(
                    bind(Key.get(item.getDeclaredTypeWithGenerics(), Config.class)),
                    item.getValue());
        }
    }

    /**
     * Bind configuration paths. Available for injection like {@code @Inject @Code("path.sub") Integer conf}.
     * Value may be null because if null values would be avoided, bindings will disappear.
     */
    private void bindValuePaths() {
        for (ConfigPath item : tree.getPaths()) {
            bindValue(
                    bind(Key.get(item.getDeclaredTypeWithGenerics(), new ConfigImpl(item.getPath()))),
                    item.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private void bindValue(final LinkedBindingBuilder binding, final Object value) {
        if (value != null) {
            binding.toInstance(value);
        } else {
            binding.toProvider(Providers.of(null));
        }
    }
}
