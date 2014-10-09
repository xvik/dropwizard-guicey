package ru.vyarus.dropwizard.guice.module.installer.feature.plugin;

import com.google.inject.Binder;
import com.google.inject.multibindings.Multibinder;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.module.installer.BindingInstaller;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils;

/**
 * Shortcut for guice multibindings mechanism.
 * Register beans annotated with {@link Plugin} annotation into set multibinding by base class
 * defined in annotation.
 * Registered set may be later injected in code as {@code Set<BaseType> plugins}.
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
    public <T> void bind(final Binder binder, final Class<? extends T> type) {
        final Class<T> pluginType = (Class<T>) type.getAnnotation(Plugin.class).value();
        Multibinder.newSetBinder(binder, pluginType).addBinding().to(type);
    }

    @Override
    public void install(final Environment environment, final Object instance) {
        // nothing to do
    }
}
