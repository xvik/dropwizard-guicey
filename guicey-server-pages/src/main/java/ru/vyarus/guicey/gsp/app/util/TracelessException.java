package ru.vyarus.guicey.gsp.app.util;

/**
 * Marker interface for special exceptions which does not contain actual stack trace (no exception was thrown
 * in user code) and used only to wrap return status code.
 *
 * @author Vyacheslav Rusakov
 * @since 29.01.2019
 * @see ru.vyarus.guicey.gsp.app.filter.AssetError
 * @see ru.vyarus.guicey.gsp.app.rest.support.TemplateRestCodeError
 */
public interface TracelessException {

    int getStatus();
}
