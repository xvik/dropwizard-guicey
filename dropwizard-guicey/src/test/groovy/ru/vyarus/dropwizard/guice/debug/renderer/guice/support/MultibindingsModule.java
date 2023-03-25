package ru.vyarus.dropwizard.guice.debug.renderer.guice.support;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.multibindings.OptionalBinder;

/**
 * @author Vyacheslav Rusakov
 * @since 21.08.2019
 */
public class MultibindingsModule extends AbstractModule {

    @Override
    protected void configure() {
        final Multibinder<Plugin> pluginMultibinder = Multibinder.newSetBinder(binder(), Plugin.class);
        pluginMultibinder.addBinding().to(MyPlugin.class);
        pluginMultibinder.addBinding().toInstance(new MyPlugin2());

        final MapBinder<String, KeyedPlugin> keyedPluginMapBinder =
                MapBinder.newMapBinder(binder(), String.class, KeyedPlugin.class);
        keyedPluginMapBinder.addBinding("foo").to(MyKeyedPlugin.class);
        keyedPluginMapBinder.addBinding("bar").toInstance(new MyKeyedPlugin2());
        
        OptionalBinder.newOptionalBinder(binder(), OptService.class).setDefault().to(DefImpl.class);
        install(new OverideModule());
    }

    public interface Plugin {}

    public static class MyPlugin implements Plugin {}
    public static class MyPlugin2 implements Plugin {}

    public interface KeyedPlugin {}

    public static class MyKeyedPlugin implements KeyedPlugin {}
    public static class MyKeyedPlugin2 implements KeyedPlugin {}

    public interface OptService {}

    public static class DefImpl implements OptService {}

    public static class ActualImpl implements OptService {}

    public static class OverideModule extends AbstractModule {
        @Override
        protected void configure() {
            OptionalBinder.newOptionalBinder(binder(), OptService.class).setBinding().to(ActualImpl.class);
        }
    }
}
