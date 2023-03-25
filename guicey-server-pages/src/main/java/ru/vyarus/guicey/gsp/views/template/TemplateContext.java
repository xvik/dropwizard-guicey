package ru.vyarus.guicey.gsp.views.template;

import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.glassfish.jersey.server.internal.process.MappableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.module.installer.util.PathUtils;
import ru.vyarus.guicey.gsp.app.asset.AssetLookup;
import ru.vyarus.guicey.gsp.app.filter.redirect.ErrorRedirect;
import ru.vyarus.guicey.gsp.app.filter.redirect.TemplateRedirect;
import ru.vyarus.guicey.gsp.app.util.ResourceLookup;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import java.net.URL;

/**
 * Contains context information for rendered template. The most useful information is original request path:
 * each template rendering request is redirected into resource (rest) and so it's impossible to know
 * original path from the request object (inside rest resource).
 * <p>
 * Template context object is thread-bound and available during template rendering request processing.
 *
 * @author Vyacheslav Rusakov
 * @since 25.10.2018
 */
public class TemplateContext {
    private final Logger logger = LoggerFactory.getLogger(TemplateContext.class);

    private final String appName;
    private final String rootUrl;
    // as rest may be mapped to sub url it is very important to know current sub url because without it it would
    // be impossible to properly resolve template (because we have only part of path and cant match extended asset
    // locations). For root matching, context will be empty
    private final String restSubContext;
    // its important to know current assumed rest prefix to properly compute path in direct template resource
    // because resource itself may be registered on any level (due to sub mappings or different application)
    private final String restPrefix;
    // called path looks like direct template call
    private final boolean directTemplate;
    private final AssetLookup assets;
    private final ErrorRedirect errorRedirect;
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private Class resourceClass;
    private String annotationTemplate;
    private boolean manualErrorHandling;

    @SuppressWarnings("checkstyle:ParameterNumber")
    public TemplateContext(final String appName,
                           final String rootUrl,
                           final String restSubContext,
                           final String restPrefix,
                           final boolean directTemplate,
                           final AssetLookup assets,
                           final ErrorRedirect errorRedirect,
                           final HttpServletRequest request,
                           final HttpServletResponse response) {
        this.appName = appName;
        this.rootUrl = rootUrl;
        this.restSubContext = restSubContext;
        this.restPrefix = restPrefix;
        this.directTemplate = directTemplate;
        this.assets = assets;
        this.errorRedirect = errorRedirect;
        this.request = request;
        this.response = response;
    }

    /**
     * @return thread bound template context instance
     */
    public static TemplateContext getInstance() {
        return Preconditions.checkNotNull(TemplateRedirect.templateContext(),
                "No template context found for current thread");
    }

    /**
     * @return server pages application name
     */
    public String getAppName() {
        return appName;
    }

    /**
     * @return root url for server pages application
     */
    public String getRootUrl() {
        return rootUrl;
    }

    /**
     * Different rest prefix may be mapped to sub context (e.g. /sub/ -&gt; com.foo.app/). If such sub context
     * detected (during rest redirection) then original url miss such sub context. In order to properly resolve
     * templates (assets may also be mapped to sub context) original path is required.
     *
     * @return current sub context mapping (after rest view, mapped to sub context, redirection) or empty string
     * if no sub context
     */
    public String getRestSubContext() {
        // just to avoid confusion, because normally context is relative
        return PathUtils.leadingSlash(restSubContext);
    }

    /**
     * Context rest mapping path. Important for direct template resource to properly identify target path
     * because direct template resource may appear on any level (due to sub mappings or mapping in other applications).
     *
     * @return rest prefix used under current template call
     */
    public String getRestPrefix() {
        // just to avoid confusion, because normally prefix is relative
        return PathUtils.leadingSlash(restPrefix);
    }

    /**
     * True means that one of registered view renderers recognize path as template file. In real life, such
     * path may be handled with special rest mapping instead, so this flag is useful only for cases when
     * no matching rest found for path (because without it it would be impossible to differentiate template not found
     * and rest path not matched cases).
     *
     * @return true if current path could be direct template call
     */
    public boolean isDirectTemplate() {
        return directTemplate;
    }

    /**
     * Each template render is redirected to rest resource so it's impossible to obtain original uri from request
     * object inside the resource.
     *
     * @return original call url
     */
    public String getUrl() {
        return getRequest().getRequestURI();
    }

    /**
     * Method may be used to access original request object (in edge cases).
     *
     * @return original request object (before any redirection)
     * @see #getUrl() for original request URI
     * @see #getRootUrl() for root mapping url
     */
    public HttpServletRequest getRequest() {
        return request;
    }

    /**
     * Raw response is required for redirection logic to avoid response processing loops
     * due to hk wrappers (if hk injection were used for response object injection it would always be a proxy).
     * <p>
     * Method may be used to handle response directly (in edge cases)
     *
     * @return original response object
     */
    public HttpServletResponse getResponse() {
        return response;
    }

    /**
     * Set resource class to check template relative to class.
     * Used by {@link ru.vyarus.guicey.gsp.app.rest.support.TemplateAnnotationFilter}.
     *
     * @param base resource class
     */
    public void setResourceClass(final Class base) {
        resourceClass = base;
    }

    /**
     * Used by {@link ru.vyarus.guicey.gsp.app.rest.support.TemplateAnnotationFilter} to set template file
     * declared in {@link Template} annotation on rest resource.
     *
     * @param template template file path
     */
    public void setAnnotationTemplate(final String template) {
        annotationTemplate = template;
    }

    /**
     * Disables GSP error pages support. Activated by presence of {@link ManualErrorHandling} annotation on resource
     * method or resource itself. Used by {@link ru.vyarus.guicey.gsp.app.rest.support.TemplateAnnotationFilter}.
     * <p>
     * May be set manually, but it is not recommended - prefer annotations usage to clearly declare "exceptions" from
     * global errors handling.
     *
     * @param manualErrors true to disable GSP errors handling, false to activate GSP error pages
     */
    public void setManualErrorHandling(final boolean manualErrors) {
        this.manualErrorHandling = manualErrors;
    }

    /**
     * Lookup relative template path either relative to resource class (if annotated with {@link Template} or
     * in one of pre-configured classpath locations. If passed template is null it will be
     * taken from {@link Template} annotation from resource class.
     * <p>
     * When provided template path is absolute - it is searched by direct location only.
     *
     * @param template template path or null
     * @return absolute path to template
     * @throws NullPointerException      if template path not set
     * @throws TemplateNotFoundException if template not found
     */
    @SuppressFBWarnings("NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE")
    public String lookupTemplatePath(@Nullable final String template) {
        String path = Strings.emptyToNull(template);
        if (path == null) {
            // from @Template annotation
            path = annotationTemplate;
        }
        Preconditions.checkNotNull(path,
                "Template name not specified neither directly in model nor in @Template annotation");

        // search relative path relative to resource class
        if (!path.startsWith(PathUtils.SLASH) && resourceClass != null) {
            final String classRelativePath = PathUtils.path(PathUtils.packagePath(resourceClass),
                    CharMatcher.is('/').trimLeadingFrom(path));
            if (assets.load(classRelativePath) != null) {
                logger.debug("Relative template '{}' found relative to {} class: '{}'",
                        template, resourceClass.getSimpleName(), path);
                // indicate absolute path
                path = PathUtils.leadingSlash(classRelativePath);
            }
        }

        // search in configured locations
        if (!path.startsWith(PathUtils.SLASH)) {
            // recover original calling path to properly resolve asset (inside sub context mapped view)
            path = PathUtils.path(restSubContext, path);
            // search in configured folders
            path = PathUtils.leadingSlash(ResourceLookup.lookupOrFail(path, assets));
            logger.debug("Relative template '{}' resolved to '{}'", template, path);
        }

        // check direct absolute path
        ResourceLookup.existsOrFail(path, assets);
        return path;
    }

    /**
     * Load asset from one of registered locations.
     * <p>
     * Method assumes to load absolute classpath location (through all custom class loaders, if registered).
     * But, if direct lookup fails, it will perform relative resolution (search in all registered
     * locations).
     * <p>
     * If custom class loaders used for assets declarations then template engines must be customized to resolve
     * templates through this method (otherwise they would not be able to find it in custom class loader).
     * For freemarker integration already provided and could be activated with
     * {@link ru.vyarus.guicey.gsp.ServerPagesBundle.ViewsBuilder#enableFreemarkerCustomClassLoadersSupport()}.
     *
     * @param path absolute or relative path
     * @return resource url or null if not found
     */
    public URL loadAsset(final String path) {
        return assets.load(path);
    }

    /**
     * Perform redirection to error page (if registered) or handle SPA route (if 404 response and SPA support enabled).
     * <p>
     * When only resulted status code is known use {@code WebApplicationException(code)} as argument for redirection.
     * <p>
     * It is safe to call redirection multiple times: only first call will be actually handled (assuming next errors
     * appear during error page rendering and can't be handled).
     * <p>
     * Method is not intended to be used directly, but could be in specific (maybe complex) edge cases.
     *
     * @param ex exception instance
     * @return true if redirect performed, false if no redirect performed
     */
    public boolean redirectError(final Throwable ex) {
        if (manualErrorHandling) {
            logger.debug("Automatic error handling disabled on path: exception assumed to be handled manually");
        }
        // use request with original uri instead of rest mapped and raw response (not hk proxy)
        // may be disabled by @ManualErrorHandling annotation
        return !manualErrorHandling && errorRedirect.redirect(getRequest(), getResponse(), wrap(ex));
    }

    private WebApplicationException wrap(final Throwable exception) {
        Throwable cause = exception;
        // compensate MappableException
        while (cause instanceof MappableException) {
            cause = cause.getCause();
        }
        return cause instanceof WebApplicationException
                ? (WebApplicationException) cause
                : new WebApplicationException(cause, 500);
    }
}
