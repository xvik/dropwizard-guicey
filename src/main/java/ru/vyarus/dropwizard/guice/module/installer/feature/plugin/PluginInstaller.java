package ru.vyarus.dropwizard.guice.module.installer.feature.plugin;

import com.google.common.base.Strings;
import com.google.inject.Binder;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.BindingInstaller;
import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils;

/**
 * Shortcut for guice multibindings mechanism.
 * Register beans annotated with {@link Plugin} annotation into set multibinding by base class
 * defined in annotation.
 * Registered set may be later injected in code as {@code Set<BaseType> plugins}.
 * <p>Optionally name attribute may be used to use map for plugins reference: {@code Map<String, BaseType>}</p>
 *
 * @author Vyacheslav Rusakov
 * @since 08.10.2014
 */
public class PluginInstaller implements FeatureInstaller<Object>, BindingInstaller {

    @Override
    public boolean matches(final Class<?> type) {
        return FeatureUtils.hasAnnotation(type, Plugin.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void install(final Binder binder, final Class<? extends T> type) {
        final Plugin annotation = type.getAnnotation(Plugin.class);
        final Class<T> pluginType = (Class<T>) annotation.value();
        final String key = Strings.emptyToNull(annotation.name());
        if (key == null) {
            Multibinder.newSetBinder(binder, pluginType).addBinding().to(type);
        } else {
            MapBinder.newMapBinder(binder, String.class, pluginType).addBinding(key).to(type);
        }

    }
}
