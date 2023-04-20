package ru.vyarus.guicey.gsp.views;

import java.util.Map;

/**
 * Dropwizard views configuration modifier. The main purpose is to provide default values for
 * exact template engine (for example, application may configure default common templates for freemarker).
 * <p>
 * View configuration is global and so only one server pages application could actually declare it
 * (usually bind from main configuration object). This modifier could be used in all server pages applications
 * applying required changes to the global configuration.
 * <p>
 * Each modifier is registered for exact renderer and so don't have to deal with map of maps (global configuration).
 *
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.guicey.gsp.app.ServerPagesAppBundle.AppBuilder#viewsConfigurationModifier(
 * String, ViewRendererConfigurationModifier)
 * @see ru.vyarus.guicey.gsp.ServerPagesBundle.ViewsBuilder#viewsConfigurationModifier(
 *  String, ViewRendererConfigurationModifier)
 * @since 06.12.2018
 */
public interface ViewRendererConfigurationModifier {

    /**
     * This map is automatically created if global configuration object did not contain it yet.
     * All modifiers targeted the same renderer will receive the same map.
     * Modifiers execution order is not predefined.
     *
     * @param config configuration map for exact renderer
     * @see ru.vyarus.guicey.gsp.ServerPagesBundle.ViewsBuilder#printViewsConfiguration()
     */
    void modify(Map<String, String> config);
}
