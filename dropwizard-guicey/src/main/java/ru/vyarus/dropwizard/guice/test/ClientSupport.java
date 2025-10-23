package ru.vyarus.dropwizard.guice.test;

import com.google.common.base.Preconditions;
import io.dropwizard.testing.DropwizardTestSupport;
import org.glassfish.jersey.client.JerseyClient;
import org.jspecify.annotations.Nullable;
import ru.vyarus.dropwizard.guice.test.client.ApacheTestClientFactory;
import ru.vyarus.dropwizard.guice.test.client.DefaultTestClientFactory;
import ru.vyarus.dropwizard.guice.test.client.ResourceClient;
import ru.vyarus.dropwizard.guice.test.client.TestClient;
import ru.vyarus.dropwizard.guice.test.client.TestClientFactory;
import ru.vyarus.dropwizard.guice.test.client.builder.TestRequestConfig;
import ru.vyarus.dropwizard.guice.url.AppUrlBuilder;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.ext.RuntimeDelegate;

/**
 * {@link JerseyClient} support for direct web tests (complete dropwizard startup).
 * <p>
 * Client support maintains single {@link JerseyClient} instance. It may be used for calling any urls (not just
 * application). Class provides many utility methods for automatic construction of base context paths, so
 * tests could be completely independent of actual configuration.
 * <p>
 * Client customization is possible through custom
 * {@link ru.vyarus.dropwizard.guice.test.client.TestClientFactory} implementation.
 * <p>
 * Dropwizard configurations for rest, app and admin contexts:
 * <ul>
 *     <li>{@code server.applicationContextPath} - app context path ("/" by default, "/application" for simple
 *     server)</li>
 *     <li>{@code server.adminContextPath} - admin context path ("/" by default, "/admin" for simple server)</li>
 *     <li>{@code server.rootPath} - rest context path ("/" by default)</li>
 * </ul>
 * The rest prefix is {@code server.applicationContextPath} + {@code server.rootPath}.
 * <p>
 * The class provides access for 4 clients (with the same api
 * {@link ru.vyarus.dropwizard.guice.test.client.TestClient}):
 * <ul>
 *     <li>{@link ru.vyarus.dropwizard.guice.test.ClientSupport} itself is a client for application root</li>
 *     <li>{@link #restClient()} specialized rest client (mapped rest path counted)</li>
 *     <li>{@link #appClient()} client for the app context (context mapping path counted)</li>
 *     <li>{@link #adminClient()} client for the admin context (context mapping path and port counted)</li>
 * </ul>
 * The main idea of clients is the ability to create a client for any base path to shorten urls in tests.
 * Also, each client could declare its own defaults, applied to all requests (for example, useful for authorization).
 * <p>
 * There is also a specialized {@link #externalClient(String, Object...)} for custom clients for creating remote api
 * clients (with the same client api):
 * {@code support.externalClient("http://localhost:8080/some/path").get("/some/resource")}.
 * <p>
 * There is a special class-based client constructor {@link #restClient(Class)} for resources. This makes tests
 * type-safe as the target resource path is obtained directly from the class annotation. Also, such clients
 * could use resource class method calls for request configuration
 * ({@link ru.vyarus.dropwizard.guice.test.client.ResourceClient#method(ru.vyarus.dropwizard.guice.url.util.Caller)}).
 * <p>
 * Note: defaults, declared on root {@link ru.vyarus.dropwizard.guice.test.ClientSupport} class would be inherited
 * by sub clients (excluding external clients).
 * <p>
 * The root client is not much useful, so the assumed usage is to get the required client and use it for actual calls.
 * <p>
 * Class also provides base urls and related jersey targets (pure jersey api):
 * <ul>
 *     <li>{@link #basePathRoot()} and {@link #target(String, Object...)}</li>
 *     <li>{@link #basePathRest()} and {@link #targetRest(String, Object...)}</li>
 *     <li>{@link #basePathApp()} and {@link #targetApp(String, Object...)}</li>
 *     <li>{@link #basePathAdmin()} and {@link #targetAdmin(String, Object...)}</li>
 * </ul>
 *
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.dropwizard.guice.test.client.TestClient
 * @since 04.05.2020
 */
public class ClientSupport extends TestClient<ClientSupport> implements AutoCloseable {

    private final DropwizardTestSupport<?> support;
    private final AppUrlBuilder urlBuilder;
    private final TestClientFactory factory;
    private JerseyClient client;

    /**
     * Create a client.
     *
     * @param support dropwizard test support
     */
    public ClientSupport(final DropwizardTestSupport<?> support) {
        this(support, null);
    }

    /**
     * Create client with custom factory.
     *
     * @param support dropwizard test support
     * @param factory custom client factory
     */
    public ClientSupport(final DropwizardTestSupport<?> support,
                         final @Nullable TestClientFactory factory) {
        this(support, factory, null);
    }

    /**
     * Create client with custom factory.
     *
     * @param support dropwizard test support
     * @param factory custom client factory
     * @param defaults defaults for all clients (could be overridden by the client-specific defaults)
     */
    public ClientSupport(final DropwizardTestSupport<?> support,
                         final @Nullable TestClientFactory factory,
                         final @Nullable TestRequestConfig defaults) {
        super(defaults);
        this.support = support;
        this.urlBuilder = new AppUrlBuilder(support::getEnvironment);
        this.factory = factory == null ? new DefaultTestClientFactory() : factory;
    }

    /**
     * Single client instance maintained within test and method will always return the same instance.
     *
     * @return client instance
     */
    public JerseyClient getClient() {
        synchronized (this) {
            if (client == null) {
                client = factory.create(support);
            }
            return client;
        }
    }

    /**
     * Shortcut to be able to quickly build an apache-connector-based client. The default urlconnector-based
     * client is better for multipart calls (apache client
     * <a href="https://github.com/eclipse-ee4j/jersey/issues/5528#issuecomment-1934766714">has problems</a>).
     * But the apache client is better for PATCH calls because the default urlconnector will complain on java &gt; 16.
     * <p>
     * With this shortcut it would be possible to use both clients in one test.
     * <p>
     * Applied defaults are inherited.
     *
     * @return client based on apache connector
     */
    public ClientSupport apacheClient() {
        return factory instanceof ApacheTestClientFactory ? this
                : new ClientSupport(support, new ApacheTestClientFactory(), defaults);
    }

    /**
     * Shortcut to be able to quickly build an apache-connector-based client. The default urlconnector-based
     * client is better for multipart calls (apache client
     * <a href="https://github.com/eclipse-ee4j/jersey/issues/5528#issuecomment-1934766714">has problems</a>).
     * But the apache client is better for PATCH calls because the default urlconnector will complain on java &gt; 16.
     * <p>
     * With this shortcut it would be possible to use both clients in one test.
     * <p>
     * Applied defaults are inherited.
     *
     * @return client based on apache connector
     */
    public ClientSupport urlconnectorClient() {
        return factory instanceof DefaultTestClientFactory ? this
                : new ClientSupport(support, new DefaultTestClientFactory(), defaults);
    }

    // -------------------------------------------------------------------------- SERVER PATHS

    /**
     * @return app context port
     * @throws NullPointerException for guicey test (when web not started)
     */
    public int getPort() {
        return support.getLocalPort();
    }

    /**
     * @return admin context port
     * @throws NullPointerException for guicey test (when web not started)
     */
    public int getAdminPort() {
        return support.getAdminPort();
    }

    /**
     * Root application path is, usually, not very interesting so consider using
     * {@link #basePathApp()}, {@link #basePathRest()} or {@link #basePathAdmin()} instead.
     *
     * @return root application path (localhost + port)
     * @throws NullPointerException for guicey test (when web not started)
     */
    public String basePathRoot() {
        return urlBuilder.root("/");
    }

    /**
     * @return base path for application context
     * @deprecated use {@link #basePathApp()}
     */
    @Deprecated
    public String basePathMain() {
        return basePathApp();
    }

    /**
     * For example, with the default configuration it would be "http://localhost:8080/". If
     * "server.applicationContextPath" is changed to "/someth", then the method will return
     * "http://localhost:8080/someth/".
     * <p>
     * Returned path will always end with a slash.
     *
     * @return base path for app context
     * @throws NullPointerException for guicey test (when web not started)
     * @see #targetApp(String, Object...)
     * @see #appClient()
     */
    public String basePathApp() {
        return urlBuilder.app("/");
    }

    /**
     * For example, with the default configuration it would be "http://localhost:8081/". For the "simple" server, it
     * would be "http://localhost:8080/adminPath/". If "server.adminContextPath" is changed to "/adm", then the method
     * will return "http://localhost:8081/adm/" for the default server and "http://localhost:8080/adm/" for "simple"
     * server.
     * <p>
     * Returned path will always end with a slash.
     *
     * @return base path for admin context
     * @throws NullPointerException for guicey test (when web not started)
     * @see #targetAdmin(String, Object...)
     * @see #adminClient()
     */
    public String basePathAdmin() {
        return urlBuilder.admin("/");
    }

    /**
     * For example, with the default configuration it would be "http://localhost:8080/". If "server.rootPath"
     * is changed to "/api", then the method will return "http://localhost:8080/api/".
     * If the app context mapping changed from root, then the returned path will count it too
     * (e.g. "http://localhost:8080/root/rest/", when "server.applicationContextPath" is "/root").
     * <p>
     * Returned path will always end with a slash.
     *
     * @return base path for rest
     * @throws NullPointerException for guicey test (when web not started)
     * @see #targetRest(String, Object...)
     * @see #restClient()
     * @see #restClient(Class)
     */
    public String basePathRest() {
        return urlBuilder.rest("/");
    }

    // ----------------------------------------------------------------------- PURE JERSEY API

    /**
     * Create a jersey {@link WebTarget} for a path, relative to application root (e.g.
     * "http://localhost:8080/" + provided path)".
     * <p>
     * It could be also used to request any external api, for example:
     * {@code target("http://somewhere.com/api/smth/").request().buildGet().invoke()}.
     * <p>
     * String format could be used to format the path:
     * {@code .target("/smth/%s/other", 12).request().buildGet().invoke()}
     * <p>
     * NOTE: could be used with guicey-only tests (when web part not started) to call any external url.
     * <p>
     * IMPORTANT: it would not apply configured defaults (like {@link #defaultHeader(String, Object)}).
     * Use builders ({@link #build(String, String, Object, Object...)}) or shortcuts
     * {@link #get(String, Class, Object...)} to cal with defaults.
     *
     * @param path target path, relative to client root or absolute external path (could contain String.format()
     *             placeholders: %s)
     * @param args variables for path placeholders (String.format() arguments)
     * @return jersey web target object
     * @see #basePathRoot()
     * @see #externalClient(String, Object...)  for external api client
     */
    @Override
    public WebTarget target(final String path, final Object... args) {
        if (isHttp(path)) {
            return getClient().target(String.format(path, args));
        }
        return super.target(path, args);
    }

    /**
     * Shortcut for {@link WebTarget} creation for the application context path.
     *
     * @param path target path, relative to the admin context (could contain String.format() placeholders: %s)
     * @param args variables for path placeholders (String.format() arguments)
     * @return jersey web target object for the admin context
     * @deprecated use {@link #targetApp(String, Object...)} instead
     */
    @Deprecated
    public WebTarget targetMain(final String path, final Object... args) {
        return targetApp(path, args);
    }

    /**
     * Shortcut for {@link WebTarget} creation for the app context path.
     * <p>
     * Example: {@code .targetApp("/path").request().buildGet().invoke()} would call "http://localhost:8080/path"
     * (or, if root context mapping changed with {@code server.applicationContextPath = "root"},
     * "http://localhost:8080/root/path").
     * <p>
     * String format could be used to format the path:
     * {@code .targetApp("/smth/%s/other", 12).request().buildGet().invoke()}
     * <p>
     * IMPORTANT: it would not apply configured defaults (like {@link #defaultHeader(String, Object)}).
     * Use app client {@link #appClient()} to call with defaults.
     *
     * @param path target path, relative to the app context (could contain String.format() placeholders: %s)
     * @param args variables for path placeholders (String.format() arguments)
     * @return jersey web target object for the app context
     * @throws NullPointerException for guicey test (when web not started)
     * @see #basePathApp() for base use construction details
     * @see #appClient()
     */
    public WebTarget targetApp(final String path, final Object... args) {
        return target(urlBuilder.app(path, args));
    }

    /**
     * Shortcut for {@link WebTarget} creation for the admin context path.
     * <p>
     * Example: {@code .targetAdmin("/path").request().buildGet().invoke()} would call "http://localhost:8081/path"
     * (or, if admin context mapping changed with {@code server.adminContextPath = "admin"},
     * "http://localhost:8081/admin/path").
     * <p>
     * String format could be used to format the path:
     * {@code .targetAdmin("/smth/%s/other", 12).request().buildGet().invoke()}
     * <p>
     * IMPORTANT: it would not apply configured defaults (like {@link #defaultHeader(String, Object)}).
     * Use admin client {@link #adminClient()} to call with defaults.
     *
     * @param path target path, relative to the admin context (could contain String.format() placeholders: %s)
     * @param args variables for path placeholders (String.format() arguments)
     * @return jersey web target object for the admin context
     * @throws NullPointerException for guicey test (when web not started)
     * @see #basePathAdmin() for base use construction details
     * @see #adminClient()
     */
    public WebTarget targetAdmin(final String path, final Object... args) {
        return target(urlBuilder.admin(path, args));
    }

    /**
     * Shortcut for {@link WebTarget} creation for the rest context path.
     * <p>
     * Example: {@code .targetRest("/path").request().buildGet().invoke()} would call "http://localhost:8080/path"
     * (or, if rest context mapping changed with {@code server.rootPath = "api"},
     * "http://localhost:8081/api/path").
     * <p>
     * String format could be used to format the path:
     * {@code .targetRest("/smth/%s/other", 12).request().buildGet().invoke()}
     * <p>
     * IMPORTANT: it would not apply configured defaults (like {@link #defaultHeader(String, Object)}).
     * Use rest client {@link #restClient()} (or specialized {@link #restClient(Class)}) to call with defaults.
     *
     * @param path target path, relative to the rest context (could contain String.format() placeholders: %s)
     * @param args variables for path placeholders (String.format() arguments)
     * @return jersey web target object for rest context
     * @throws NullPointerException for guicey test (when web not started)
     * @see #basePathRest() for base use construction details
     * @see #restClient()
     */
    public WebTarget targetRest(final String path, final Object... args) {
        return target(urlBuilder.rest(path, args));
    }

    // ------------------------------------------------------------------------------ CLIENTS

    /**
     * Construct a rest client. Client would be configured with the current rest path root
     * (@link {@link #basePathRest()}), so requests would need to use only relative paths.
     * <p>
     * Typed rest client could be obtained from this generic client: {@code restClient().subClient(Resource.class)};
     * <p>
     * Inherits current defaults (like {@link #defaultHeader(String, Object)}.
     *
     * @return rest client
     */
    public TestClient<?> restClient() {
        // client INHERITS support defaults
        return new TestClient<>(() -> getClient().target(basePathRest()), defaults);
    }

    /**
     * Construct a specialized rest client for the given resource class. Client would be configured with the current
     * rest path root (@link {@link #basePathRest()}) together with the resource class path (obtained from annotation),
     * so requests would need to use paths, relative to resource.
     * <p>
     * Also, such client provide methods to configure requests direct from resource methods
     * {@link ResourceClient#method(ru.vyarus.dropwizard.guice.url.util.Caller)}.
     * <p>
     * Inherits current defaults (like {@link #defaultHeader(String, Object)}.
     *
     * @param resource resource class to configure target url from
     * @param <K>      resource type
     * @return rest client for the given resource class
     */
    @Override
    public <K> ResourceClient<K> restClient(final Class<K> resource) {
        final String target = RuntimeDelegate.getInstance().createUriBuilder().path(resource).toTemplate();
        // client INHERITS support defaults
        return new ResourceClient<>(() -> getClient().target(basePathRest()).path(target), defaults, resource);
    }

    /**
     * Construct a client for the app context. Client would be configured with the current app context path root
     * (@link {@link #basePathApp()}), so requests would need to use only relative paths.
     * <p>
     * Note: by default, root client {@link ru.vyarus.dropwizard.guice.test.ClientSupport} itself and
     * app context client would be the same, because usually app context is "/". But, you can still prefer
     * app client to shield from potential app context path changes.
     * <p>
     * Inherits current defaults (like {@link #defaultHeader(String, Object)}.
     *
     * @return app client
     */
    public TestClient<?> appClient() {
        // client INHERITS support defaults
        return new TestClient<>(() -> getClient().target(basePathApp()), defaults);
    }

    /**
     * Construct a client for the admin context. Client would be configured with the current admin context path root
     * (@link {@link #basePathAdmin()}), so requests would need to use only relative paths.
     * <p>
     * Inherits current defaults (like {@link #defaultHeader(String, Object)}.
     *
     * @return admin client
     */
    public TestClient<?> adminClient() {
        // client INHERITS support defaults
        return new TestClient<>(() -> getClient().target(basePathAdmin()), defaults);
    }

    /**
     * Construct a client for external url.
     * <p>
     * Example of variables usage: {@code externalClient("http://localhost:8080/%s/other", 12)}.
     * <p>
     * Will not inherit current defaults.
     *
     * @param url  external url, started with "http(s)" (could contain String.format() placeholders: %s)
     * @param args variables for path placeholders (String.format() arguments)
     * @return client for the given external url
     */
    public TestClient<?> externalClient(final String url, final Object... args) {
        checkHttp(url);
        // custom external client - no defaults inherited
        return new TestClient<>(() -> getClient().target(String.format(url, args)), null);
    }

    @Override
    public void close() throws Exception {
        synchronized (this) {
            if (client != null) {
                client.close();
                client = null;
            }
        }
    }

    @Override
    protected WebTarget getRoot() {
        return getClient().target(basePathRoot());
    }

    private boolean isHttp(final String path) {
        return path.toLowerCase().startsWith("http");
    }

    private void checkHttp(final String host) {
        Preconditions.checkState(isHttp(host),
                "Host must include target server protocol and the host name (like http://myhost.com)");
    }
}
