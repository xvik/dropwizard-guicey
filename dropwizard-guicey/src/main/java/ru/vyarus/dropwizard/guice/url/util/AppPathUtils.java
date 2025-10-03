package ru.vyarus.dropwizard.guice.url.util;

import com.google.common.base.Preconditions;
import io.dropwizard.core.setup.Environment;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.ServerConnector;
import ru.vyarus.dropwizard.guice.module.installer.util.PathUtils;

/**
 * Utilities to resolve url-related server configuration (ports, mappings).
 *
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.dropwizard.guice.url.AppUrlBuilder
 * @since 26.09.2025
 */
public final class AppPathUtils {

    private AppPathUtils() {
    }

    /**
     * Get main application context port.
     *
     * @param environment environment instance
     * @return the actual port the connector is listening to, or -1 if it has not been opened, or -2 if it has been
     * closed.
     */
    public static int getAppPort(final Environment environment) {
        return getAppConnector(environment).getLocalPort();
    }

    /**
     * Get admin application context port.
     *
     * @param environment environment instance
     * @return the actual port the connector is listening to, or -1 if it has not been opened, or -2 if it has been
     * closed.
     */
    public static int getAdminPort(final Environment environment) {
        return getAdminConnector(environment).getLocalPort();
    }

    /**
     * Rest context is configured with {@code server.rootPath} and it is "/" by default.
     * Also, rest is mapped under the main context, so if main context mapping changed with
     * {@code server.applicationContextPath} it must also be counted.
     * The returned path counts both and so is relative to the server root.
     *
     * @param environment environment instance
     * @return rest context (with a context path), relative to the server root path
     */
    public static String getRestContextPath(final Environment environment) {
        final String contextPath = Preconditions.checkNotNull(environment.getJerseyServletContainer(),
                        "No started web application")
                .getServletConfig().getServletContext().getContextPath();
        // server.rootPath
        final String restMapping = PathUtils.trailingSlash(PathUtils.trimStars(environment.jersey().getUrlPattern()));
        return PathUtils.trailingSlash(
                PathUtils.path(contextPath, restMapping));
    }

    /**
     * Main context is configured with {@code server.applicationContextPath} and it is "/" by default.
     *
     * @param environment environment instance
     * @return main context (with a context path), relative to the server root path
     */
    public static String getAppContextPath(final Environment environment) {
        return PathUtils.trailingSlash(PathUtils.path(environment.getApplicationContext().getContextPath()));
    }

    /**
     * Admin context is configured with {@code server.adminContextPath} and it is "/" by default
     * ("/admin" for simple server). Note that by default, admin is on different port.
     *
     * @param environment environment instance
     * @return main context (with a context path), relative to the server root path
     */
    public static String getAdminContextPath(final Environment environment) {
        return PathUtils.trailingSlash(PathUtils.path(environment.getAdminContext().getContextPath()));
    }

    /**
     * Get the root application url (note: it could be different from rest or main context!). Assumed application is
     * accessible by configured url without a proxy. So for default server config, the call
     * {@code getRootUrl("http://myhost.com", environment)} will return "http://myhost:8080/".
     * <p>
     * If application is behind a proxy, hiding application port and, for example, applying some prefix
     * (e.g., apache redirect "http://myhost.com/my-app" to "http://localhost:8080/"), then this method can't be used -
     * use proxy prefix directly: {@code "http://myhost.com/my-app/"}.
     *
     * @param host        base host, including protocol and host name (like http://myhost.com)
     * @param environment environment instance
     * @return root application url
     */
    public static String getRooUrl(final String host, final Environment environment) {
        return formatUrl(host, getAppPort(environment), "/");
    }

    /**
     * Get the main application url. Assumed application is accessible by configured url without a proxy.
     * So for default server config, the call {@code getMainUrl("http://myhost.com", environment)}
     * will return "http://myhost:8080/".
     * <p>
     * If application is behind a proxy, hiding application port and, for example, applying some prefix
     * (e.g., apache redirect "http://myhost.com/my-app" to "http://localhost:8080/"), then this method can't be used -
     * use only prefix in this case: {@code "http://myhost.com/my-app" + getMainContextPath(environment)}.
     *
     * @param host        base host, including protocol and host name (like http://myhost.com)
     * @param environment environment instance
     * @return main application url (with a context path)
     */
    public static String getAppUrl(final String host, final Environment environment) {
        return formatUrl(host, getAppPort(environment), getAppContextPath(environment));
    }

    /**
     * Get the admin application url. Assumed application is accessible by configured url without a proxy.
     * So for default server config, the call {@code getAdminUrl("http://myhost.com", environment)}
     * will return "http://myhost:8081/" (for simple server: "http://myhost:8080/admin").
     * <p>
     * If application is behind a proxy, hiding application port and, for example, applying some prefix
     * (e.g., apache redirect "http://myhost.com/my-app" to "http://localhost:8080/"), then this method can't be used -
     * use only prefix in this case: {@code "http://myhost.com/my-app" + getAdminContextPath(environment)}.
     *
     * @param host        base host, including protocol and host name (like http://myhost.com)
     * @param environment environment instance
     * @return admin application url (with a context path)
     */
    public static String getAdminUrl(final String host, final Environment environment) {
        return formatUrl(host, getAdminPort(environment), getAdminContextPath(environment));
    }

    /**
     * Get the rest application url. Assumed application is accessible by configured url without a proxy.
     * So for default server config, the call {@code getRestUrl("http://myhost.com", environment)}
     * will return "http://myhost:8080/".
     * <p>
     * If application is behind a proxy, hiding application port and, for example, applying some prefix
     * (e.g., apache redirect "http://myhost.com/my-app" to "http://localhost:8080/"), then this method can't be used -
     * use only prefix in this case: {@code "http://myhost.com/my-app" + getRestContextPath(environment)}.
     *
     * @param host        base host, including protocol and host name (like http://myhost.com)
     * @param environment environment instance
     * @return rest application url (with a context path)
     */
    public static String getRestUrl(final String host, final Environment environment) {
        return formatUrl(host, getAppPort(environment), getRestContextPath(environment));
    }

    private static ServerConnector getAppConnector(final Environment environment) {
        return (ServerConnector) getConnectors(environment)[0];
    }

    private static ServerConnector getAdminConnector(final Environment environment) {
        final Connector[] connectors = getConnectors(environment);
        return ((ServerConnector) connectors[connectors.length - 1]);
    }

    private static Connector[] getConnectors(final Environment environment) {
        return Preconditions.checkNotNull(environment.getApplicationContext().getServer(),
                        "No started web application").getConnectors();
    }

    private static String formatUrl(final String host, final int port, final String context) {
        Preconditions.checkState(host.toLowerCase().startsWith("http"),
                "Host must include target server protocol and the host name (like http://myhost.com)");
        return PathUtils.path(String.format("%s:%s", host, port), context);
    }
}
