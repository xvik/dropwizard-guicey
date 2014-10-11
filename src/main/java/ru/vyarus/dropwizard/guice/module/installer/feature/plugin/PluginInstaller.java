package ru.vyarus.dropwizard.guice.module.installer.feature.plugin;

import com.google.inject.Binder;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.BindingInstaller;
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
public class PluginInstaller implements FeatureInstaller<Object>, BindingInstaller {

    @Override
    public boolean matches(final Class<?> type) {
        return FeatureUtils.hasAnnotation(type, Plugin.class)
                || FeatureUtils.hasAnnotatedAnnotation(type, Plugin.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void install(final Binder binder, final Class<? extends T> type) {
        final Plugin annotation = FeatureUtils.getAnnotation(type, Plugin.class);
        if (annotation != null) {
            Multibinder.newSetBinder(binder, (Class<T>) annotation.value()).addBinding().to(type);
        } else {
            final Annotation namesAnnotation = FeatureUtils.getAnnotatedAnnotation(type, Plugin.class);
            final Method valueMethod = FeatureUtils.findMethod(namesAnnotation.annotationType(), "value");
            final Class<Object> keyType = (Class<Object>) valueMethod.getReturnType();
            final Object key = FeatureUtils.invokeMethod(valueMethod, namesAnnotation);
            final Plugin pluginAnnotation = namesAnnotation.annotationType().getAnnotation(Plugin.class);
            registerNamedPlugin(binder, (Class<Object>) pluginAnnotation.value(), keyType, type, key);
        }
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private <T, K> void registerNamedPlugin(final Binder binder, final Class<T> pluginType, final Class<K> keyType,
                                            final Class<? extends T> plugin, final K key) {
        MapBinder.newMapBinder(binder, keyType, pluginType).addBinding(key).to(plugin);
    }
}
