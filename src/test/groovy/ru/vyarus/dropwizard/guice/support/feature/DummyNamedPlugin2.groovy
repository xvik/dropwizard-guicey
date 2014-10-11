package ru.vyarus.dropwizard.guice.support.feature

import ru.vyarus.dropwizard.guice.module.installer.feature.plugin.Plugin

/**
 * @author Vyacheslav Rusakov 
 * @since 11.10.2014
 */
@Plugin(value = PluginInterface, name = 'plugin2')
class DummyNamedPlugin2 implements PluginInterface{
}
