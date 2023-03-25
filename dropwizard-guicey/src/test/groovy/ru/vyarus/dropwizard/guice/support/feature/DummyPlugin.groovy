package ru.vyarus.dropwizard.guice.support.feature

import ru.vyarus.dropwizard.guice.module.installer.feature.plugin.Plugin

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Annotation to define {@code PluginInterface} plugins to later be able to inject all plugins as
 * {@code Map<DummyPluginKey, PluginInterface>}.
 * @author Vyacheslav Rusakov 
 * @since 11.10.2014
 */
@Plugin(PluginInterface)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DummyPlugin {
    DummyPluginKey value();
}