package ru.vyarus.guicey.gsp.app.filter;

import ru.vyarus.guicey.gsp.app.util.TracelessException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;

/**
 * Exception indicates error serving asset (static resource) within {@link ServerPagesFilter}.
 * In most cases it would mean 404 error (resource not found).
 * <p>
 * Custom exception type used only to simplify asset error detection (differentiate from rest errors).
 *
 * @author Vyacheslav Rusakov
 * @see ServerPagesFilter
 * @since 29.01.2019
 */
public class AssetError extends WebApplicationException implements TracelessException {

    public AssetError(final HttpServletRequest request, final int status) {
        super("Error serving asset " + request.getRequestURI() + ": " + status, status);
    }

    @Override
    public int getStatus() {
        return getResponse().getStatus();
    }
}
