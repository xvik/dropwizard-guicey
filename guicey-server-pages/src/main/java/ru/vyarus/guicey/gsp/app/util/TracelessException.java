package ru.vyarus.guicey.gsp.app.util;

/**
 * Marker interface for special exceptions which does not contain actual stack trace (no exception was thrown
 * in user code) and used only to wrap return status code.
 *
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.guicey.gsp.app.filter.AssetError
 * @see ru.vyarus.guicey.gsp.app.rest.support.TemplateRestCodeError
 * @since 29.01.2019
 */
public interface TracelessException {

    /**
     * @return status code
     */
    int getStatus();
}
