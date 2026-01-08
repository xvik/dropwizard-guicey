package ru.vyarus.dropwizard.guice.url;

import io.dropwizard.core.setup.Environment;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.ws.rs.core.UriBuilder;
import org.glassfish.jersey.internal.inject.InjectionManager;
import ru.vyarus.dropwizard.guice.injector.lookup.GuiceBeanProvider;
import ru.vyarus.dropwizard.guice.module.installer.util.PathUtils;
import ru.vyarus.dropwizard.guice.url.util.AppPathUtils;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * General utility for building absolute application urls (counting current server configuration).
 * This might be used for views, when (especially when proxy is used) it is often required to build absolute
 * urls. Also, could be usd for resource redirections (but, in this case,
 * {@link ru.vyarus.dropwizard.guice.url.RestPathBuilder} could be used directly).
 * <p>
 * The default server creates two contexts: main (including rest) on 8080 and admin context on 8081.
 * For simple server, there would be only one context where admin would be available on sub-url "/admin".
 * <p>
 * Url-related configurations:
 * <ul>
 *     <li>{@code server.applicationContextPath} - main context path</li>
 *     <li>{@code server.rootPath} - rest mapping url (main context path is also applied for rest)</li>
 *     <li>{@code server.adminContextPath} - admin context path</li>
 * </ul>
 * <p>
 * By default, class would create "localhost" based urls (because there is no way to know server host).
 * To build for a particular host use {@link #forHost(String)} - this way server ports will be applied.
 * <p>
 * Application could also be behind a proxy (like apache) which hides real server port. In this case use
 * {@link #forProxy(String)} - server port would not be applied. Also, apache might be configured to
 * serve application for a sub-path like "http://myhost.com/myapp/" - use this as a proxy url to build
 * correct application urls.
 *
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.dropwizard.guice.url.util.AppPathUtils for configured ports and mappings direct resolution
 * @see ru.vyarus.dropwizard.guice.url.RestPathBuilder for resource-only urls (not absolute)
 * @since 26.09.2025
 */
public class AppUrlBuilder {

    private final Supplier<Environment> environment;
    private final Provider<InjectionManager> injectorProvider;
    private final String host;
    private final boolean proxied;

    /**
     * Default constructor for "localhost"-based urls. It would be possible to create specialized builders with
     * {@link #forHost(String)} or {@link #forProxy(String)} methods.
     * <p>
     * Note: method annotated with {@link jakarta.inject.Inject} to be able to obtain builder directly from
     * guice context.
     *
     * @param environment environment instance
     */
    @Inject
    public AppUrlBuilder(final Environment environment) {
        this(() -> environment);
    }

    /**
     * Special constructor for instance creation before application startup.
     *
     * @param environment environment supplier
     */
    public AppUrlBuilder(final Supplier<Environment> environment) {
        this(environment, "http://localhost", false);
    }

    /**
     * Create an app url builder.
     *
     * @param environment environment object
     * @param host        required host
     * @param proxied     true if server is proxied (port already declared in host)
     */
    protected AppUrlBuilder(final Supplier<Environment> environment, final String host, final boolean proxied) {
        this.environment = environment;
        this.host = host;
        this.proxied = proxied;
        this.injectorProvider = GuiceBeanProvider.provide(InjectionManager.class).forEnv(environment);
    }

    /**
     * Create builder instance for specific host. The host must contain protocol and the host name like
     * "https://myhost.com" and builder will append correct port and build urls relatively.
     *
     * @param host application host (and protocol)
     * @return builder instance
     */
    public AppUrlBuilder forHost(final String host) {
        return new AppUrlBuilder(environment, host, false);
    }

    /**
     * Create builder instance for proxied application. In this case, real application port is not used
     * (most commonly, apache would redirect port 80 into application port). Also, apache could be configured
     * to serve application from a sub-path like "http://myhost.com/myapp/" (provided host myst point to the
     * application root).
     *
     * @param proxy proxied host (and protocol)
     * @return builder instance
     */
    public AppUrlBuilder forProxy(final String proxy) {
        return new AppUrlBuilder(environment, proxy, true);
    }


    // ----------------------------------------------------------------------------------- BUILDER METHODS

    /**
     * Build url relative to application root ("/"). No server configurations applied in this case.
     * <p>
     * Warning: don't mix root urls with the main context urls as application main context could differ.
     * <p>
     * To get root url: {@code #root("/")},
     *
     * @param path target path, relative to application root (could contain String.format() placeholders: %s)
     * @param args variables for path placeholders (String.format() arguments)
     * @return application path url
     */
    public String root(final String path, final Object... args) {
        final String root = proxied ? host
                : AppPathUtils.getRooUrl(host, environment.get());
        return PathUtils.path(root, String.format(path, args));
    }

    /**
     * Build url relative to application rest mapping. Configured main context path
     * {@code server.applicationContextPath} and rest path {@code server.rootPath} would be prepended automatically.
     * <p>
     * To get rest root url: {@code #rest("/")},
     *
     * @param path target path, relative to rest root (could contain String.format() placeholders: %s)
     * @param args variables for path placeholders (String.format() arguments)
     * @return application resource url
     * @see #rest(Class) for class-based resource url declaration
     */
    public String rest(final String path, final Object... args) {
        return PathUtils.path(baseRest(), String.format(path, args));
    }

    /**
     * Build url relative to application rest mapping. Configured main context path
     * {@code server.applicationContextPath} and rest path {@code server.rootPath} would be prepended automatically.
     * <p>
     * Resource path is applied from resource {@link jakarta.ws.rs.Path} annotation. Resource method path could
     * be declared with a method call:
     * {@link ru.vyarus.dropwizard.guice.url.RestPathBuilder#method(ru.vyarus.dropwizard.guice.url.util.Caller)}
     * (in this case all non-null arguments mapped as query and path params would be applied automatically). If
     * resource-method-based declaration not used, then it might be required to provide manually values for declared
     * path params ({@link ru.vyarus.dropwizard.guice.url.resource.ResourceParamsBuilder#pathParam(String, Object)})
     * and (optionally) apply query params.
     * <p>
     * Example:
     * {@code rest(MyResource.class).method(res -> res.doGet("abc", 12).build())}, where
     * {@code public Response doGet(@PathParam("par1") String par1, @QueryParam("qp") int qp} and so
     * path param "par1" and query param "qp" would be registered automatically (assuming path param used in resource
     * path).
     * <p>
     * If target method is in sub-resource, use
     * {@link ru.vyarus.dropwizard.guice.url.RestPathBuilder#subResource(String, Class, Object...)} to append its path
     * and switch context class.
     * <p>
     * Direct resource class and method usage allows to get refactoring-safe urls.
     *
     * @param resource resource class
     * @param <K>      resource type
     * @return resource path builder
     */
    public <K> RestPathBuilder<K> rest(final Class<K> resource) {
        return new RestPathBuilder<>(baseRest(), injectorProvider, resource, false);
    }

    /**
     * Manual rest path building. Useful for complex cases with matrix params in the middle.
     *
     * @param path     path builder
     * @param resource target resource class
     * @param <K>      resource type
     * @return resource path builder
     */
    public <K> RestPathBuilder<K> rest(final Consumer<UriBuilder> path, final Class<K> resource) {
        final UriBuilder builder = UriBuilder.newInstance();
        path.accept(builder);
        // resource Path annotation is ignored
        return new RestPathBuilder<>(rest(builder.toString()), injectorProvider, resource, true);
    }

    /**
     * Build url relative to the main context ({@code server.applicationContextPath}).
     * <p>
     * To get main context root url: {@code #main("/")},
     *
     * @param path target path, relative to the main context (could contain String.format() placeholders: %s)
     * @param args variables for path placeholders (String.format() arguments)
     * @return application path url
     */
    public String app(final String path, final Object... args) {
        final String main = proxied
                ? PathUtils.path(host, getAppContextPath())
                : AppPathUtils.getAppUrl(host, environment.get());
        return PathUtils.path(main, String.format(path, args));
    }

    /**
     * Build url relative to the admin context ({@code server.adminContextPath}).
     * <p>
     * To get admin context root url: {@code #admin("/")},
     *
     * @param path target path, relative to the admin context (could contain String.format() placeholders: %s)
     * @param args variables for path placeholders (String.format() arguments)
     * @return application path url
     */
    public String admin(final String path, final Object... args) {
        final String admin = proxied
                ? PathUtils.path(host, getAdminContextPath())
                : AppPathUtils.getAdminUrl(host, environment.get());
        return PathUtils.path(admin, String.format(path, args));
    }

    // ----------------------------------------------------------------------------------- UTILS

    /**
     * Get main application context port.
     *
     * @return the actual port the connector is listening to, or -1 if it has not been opened, or -2 if it has been
     * closed.
     */
    public int getAppPort() {
        return AppPathUtils.getAppPort(environment.get());
    }

    /**
     * Get admin application context port.
     *
     * @return the actual port the connector is listening to, or -1 if it has not been opened, or -2 if it has been
     * closed.
     */
    public int getAdminPort() {
        return AppPathUtils.getAdminPort(environment.get());
    }

    /**
     * Rest context is configured with {@code server.rootPath} and it is "/" by default.
     * Also, rest is mapped under the main context, so if main context mapping changed with
     * {@code server.applicationContextPath} it must also be counted.
     * The returned path counts both and so is relative to the server root.
     *
     * @return rest context (with a context path), relative to the server root path
     */
    public String getRestContextPath() {
        return AppPathUtils.getRestContextPath(environment.get());
    }

    /**
     * Main context is configured with {@code server.applicationContextPath} and it is "/" by default.
     *
     * @return main context (with a context path), relative to the server root path
     */
    public String getAppContextPath() {
        return AppPathUtils.getAppContextPath(environment.get());
    }

    /**
     * Admin context is configured with {@code server.adminContextPath} and it is "/" by default
     * ("/admin" for simple server). Note that by default, admin is on different port.
     *
     * @return main context (with a context path), relative to the server root path
     */
    public String getAdminContextPath() {
        return AppPathUtils.getAdminContextPath(environment.get());
    }

    private String baseRest() {
        return proxied
                ? PathUtils.path(host, getRestContextPath())
                : AppPathUtils.getRestUrl(host, environment.get());
    }

}
