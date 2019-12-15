package ru.vyarus.dropwizard.guice.module.installer.feature.plugin;

import com.google.common.base.Preconditions;
import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.binding.BindingInstaller;
import ru.vyarus.dropwizard.guice.module.installer.order.Order;
import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Shortcut for guice multibindings mechanism.
 * Register beans annotated with {@link Plugin} annotation into set multibinding by base class
 * defined in annotation.
 * Registered set may be later injected in code as {@code Set<BaseType> plugins}.
 * <p>To use {@code Map<String, BaseType>} create new annotation, annotated with {@code @Plugin}.
 * Use new annotation to define plugins. It's value attribute will be used as key (this way you can use
 * different enums for different plugin types and not need to always write plugin interface.</p>
 *
 * @author Vyacheslav Rusakov
 * @since 08.10.2014
 */
@Order(80)
public class PluginInstaller implements FeatureInstaller, BindingInstaller {

    private final PluginReporter reporter = new PluginReporter();

    @Override
    public boolean matches(final Class<?> type) {
        return FeatureUtils.hasAnnotation(type, Plugin.class)
                || FeatureUtils.hasAnnotatedAnnotation(type, Plugin.class);
    }

    @Override
    public void bind(final Binder binder, final Class<?> type, final boolean lazy) {
        Preconditions.checkArgument(!lazy, "Plugin bean can't be lazy: %s", type.getName());
        registerPlugin(binder, type);
    }

    @Override
    public <T> void manualBinding(final Binder binder, final Class<T> type, final Binding<T> binding) {
        registerPlugin(binder, type);
    }

    @SuppressWarnings("unchecked")
    public void registerPlugin(final Binder binder, final Class<?> type) {
        // multibindings registration (common for both registration types)
        final Plugin annotation = FeatureUtils.getAnnotation(type, Plugin.class);
        if (annotation != null) {
            final Class pluginType = annotation.value();
            reporter.simple(pluginType, type);
            Multibinder.newSetBinder(binder, pluginType).addBinding().to(type);
        } else {
            /*
                @Plugin(PluginInterface)
                public @interface CustomPluginAnnotation {
                    PluginKey value();
                }
             */
            final Annotation namesAnnotation = FeatureUtils.getAnnotatedAnnotation(type, Plugin.class);
            final Method valueMethod = FeatureUtils.findMethod(namesAnnotation.annotationType(), "value");
            final Class<Object> keyType = (Class<Object>) valueMethod.getReturnType();
            final Object key = FeatureUtils.invokeMethod(valueMethod, namesAnnotation);
            final Plugin pluginAnnotation = namesAnnotation.annotationType().getAnnotation(Plugin.class);
            final Class<Object> pluginType = (Class<Object>) pluginAnnotation.value();
            reporter.named(keyType, pluginType, key, type);
            registerNamedPlugin(binder, pluginType, keyType, type, key);
        }
    }

    private <T, K> void registerNamedPlugin(final Binder binder, final Class<T> pluginType, final Class<K> keyType,
                                            final Class<? extends T> plugin, final K key) {
        MapBinder.newMapBinder(binder, keyType, pluginType).addBinding(key).to(plugin);
    }

    @Override
    public void report() {
        reporter.report();
    }
}
