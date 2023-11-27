package ru.vyarus.dropwizard.guice.test;

import com.google.common.base.Preconditions;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.DropwizardTestSupport;
import javax.annotation.Nullable;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.jersey.client.JerseyClient;
import ru.vyarus.dropwizard.guice.module.installer.util.PathUtils;
import ru.vyarus.dropwizard.guice.test.client.DefaultTestClientFactory;
import ru.vyarus.dropwizard.guice.test.client.TestClientFactory;

import java.util.Arrays;
import java.util.function.Supplier;

/**
 * {@link JerseyClient} support for direct web tests (complete dropwizard startup).
 * <p>
 * Client support maintains single {@link JerseyClient} instance. It may be used for calling any urls (not just
 * application). Class provides many utility methods for automatic construction of base context paths, so
 * tests could be completely independent from actual configuration.
 * <p>
 * Client customization is possible through custom
 * {@link ru.vyarus.dropwizard.guice.test.client.TestClientFactory} implementation.
 * <p>
 * See {@link #get(String, Class)}, {@link #post(String, Object, Class)} and other simple methods as client api
 * usage example (or use them directly if appropriate).
 *
 * @author Vyacheslav Rusakov
 * @since 04.05.2020
 */
public class ClientSupport implements AutoCloseable {
    private static final String HTTP_LOCALHOST = "http://localhost:";

    private final DropwizardTestSupport<?> support;
    private final TestClientFactory factory;
    private JerseyClient client;

    public ClientSupport(final DropwizardTestSupport<?> support) {
        this(support, null);
    }

    public ClientSupport(final DropwizardTestSupport<?> support,
                         final @Nullable TestClientFactory factory) {
        this.support = support;
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
     * @return main context port
     * @throws NullPointerException for guicey test
     */
    public int getPort() {
        return support.getLocalPort();
    }

    /**
     * @return admin context port
     * @throws NullPointerException for guicey test
     */
    public int getAdminPort() {
        return support.getAdminPort();
    }

    /**
     * @return root application path (localhost + port)
     */
    public String basePathRoot() {
        return PathUtils.trailingSlash(PathUtils.path(HTTP_LOCALHOST + getPort()));
    }

    /**
     * For example, with default configuration it would be "http://localhost:8080/". If "server.applicationContextPath"
     * would be changed to "/someth" then method will return "http://localhost:8080/someth/".
     * <p>
     * Returned path will always end with slash.
     *
     * @return base path for application main context
     * @throws NullPointerException for guicey test
     */
    public String basePathMain() {
        final String contextMapping = support.getEnvironment().getApplicationContext().getContextPath();
        return PathUtils.trailingSlash(
                PathUtils.path(basePathRoot(), contextMapping));
    }

    /**
     * For example, with the default configuration it would be "http://localhost:8081/". For "simple" server it would
     * be "http://localhost:8080/adminPath/".
     * <p>
     * Returned path will always end with slash.
     *
     * @return base path for admin context
     * @throws NullPointerException for guicey test
     */
    public String basePathAdmin() {
        final String contextMapping = support.getEnvironment().getAdminContext().getContextPath();
        return PathUtils.trailingSlash(
                PathUtils.path(HTTP_LOCALHOST + getAdminPort(), contextMapping));
    }

    /**
     * For example, with the default configuration it would be "http://localhost:8080/". If "server.rootPath"
     * would be changed to "/someth" then method will return "http://localhost:8080/someth/".
     * If main context mapping changed from root, then returned path will count it too
     * (e.g. "http://localhost:8080/root/rest/", when "server.applicationContextPath" is "/root").
     * <p>
     * Returned path will always end with slash.
     *
     * @return base path for rest
     * @throws NullPointerException for guicey test
     */
    public String basePathRest() {
        final Environment env = support.getEnvironment();
        final String contextPath = env.getJerseyServletContainer()
                .getServletConfig().getServletContext().getContextPath();
        // server.rootPath
        final String restMapping = PathUtils.trailingSlash(PathUtils.trimStars(env.jersey().getUrlPattern()));
        return PathUtils.trailingSlash(
                PathUtils.path(basePathRoot(), contextPath, restMapping));
    }

    /**
     * Unbounded (universal) {@link WebTarget} construction shortcut. First url part must contain host (port) target.
     * When multiple parameters provided, they are connected with "/", avoiding duplicate slash appearances
     * so, for example, "app, path", "app/, /path" or any other variation would always lead to correct "app/path").
     * Essentially this is the same as using {@link WebTarget#path(String)} multiple times (after initial target
     * creation).
     * <p>
     * Example: {@code .target("http://localhotst:8080/smth/").request().buildGet().invoke()}
     * <p>
     * NOTE: safe to use with guicey-only tests (when web part not started) to call any external url.
     *
     * @param paths one or more path parts (joined with '/')
     * @return jersey web target object
     */
    public WebTarget target(final String... paths) {
        Preconditions.checkState(paths.length != 0,
                "Target required (e.g. http://localhost:8080/)");
        return getClient().target(PathUtils.path(paths));
    }

    /**
     * Shortcut for {@link WebTarget} creation for main context path. Method abstracts you from actual configuration
     * so you can just call servlets by their registration uri.
     * <p>
     * Without parameters it will target main context root: {@code .targetMain().request().buildGet().invoke()} would
     * call "http://localhost:8080/".
     * <p>
     * Additional paths may be provided to construct urls:
     * {@code .targetMain("something").request().buildGet().invoke()} would call "http://localhost:8080/something"
     * and {@code .targetMain("foo", "bar").request().buildGet().invoke()} would call "http://localhost:8080/foo/bar".
     * Last example is equivalent to jersey api (kind of shortcut):
     * {@code .targetMain().path("foo").path("bar").request().buildGet().invoke()}.
     *
     * @param paths zero, one or more path parts (joined with '/') and appended to base path
     * @return jersey web target object for main context
     * @throws NullPointerException for guicey test
     * @see #basePathMain() for base use construction details
     */
    public WebTarget targetMain(final String... paths) {
        return target(merge(basePathMain(), paths));
    }

    /**
     * Shortcut for {@link WebTarget} creation for admin context path. Method abstracts you from actual configuration
     * so you can just call servlets by their registration uri.
     * <p>
     * Without parameters it will target admin context root: {@code .targetAdmin().request().buildGet().invoke()} would
     * call "http://localhost:8081/". For simple server it would be "http://localhost:8080/admin/".
     * <p>
     * Additional paths may be provided to construct urls:
     * {@code .targetAdmin("something").request().buildGet().invoke()} would call "http://localhost:8081/something"
     * and {@code .targetAdmin("foo", "bar").request().buildGet().invoke()} would call "http://localhost:8081/foo/bar".
     * Last example is equivalent to jersey api (kind of shortcut):
     * {@code .targetAdmin().path("foo").path("bar").request().buildGet().invoke()}.
     *
     * @param paths zero, one or more path parts (joined with '/') and appended to base path
     * @return jersey web target object for admin context
     * @throws NullPointerException for guicey test
     * @see #basePathAdmin() for base use construction details
     */
    public WebTarget targetAdmin(final String... paths) {
        return target(merge(basePathAdmin(), paths));
    }

    /**
     * Shortcut for {@link WebTarget} creation for rest context path. Method abstracts you from actual configuration
     * so you can just call rest resources by their registration uri.
     * <p>
     * Without parameters it will target rest context root: {@code .targetRest().request().buildGet().invoke()} would
     * call "http://localhost:8080/".
     * <p>
     * Additional paths may be provided to construct urls:
     * {@code .targetRest("something").request().buildGet().invoke()} would call "http://localhost:8080/something"
     * and {@code .targetRest("foo", "bar").request().buildGet().invoke()} would call "http://localhost:8080/foo/bar".
     * Last example is equivalent to jersey api (kind of shortcut):
     * {@code .targetRest().path("foo").path("bar").request().buildGet().invoke()}.
     *
     * @param paths zero, one or more path parts (joined with '/') and appended to base path
     * @return jersey web target object for rest context
     * @throws NullPointerException for guicey test
     * @see #basePathRest() for base use construction details
     */
    public WebTarget targetRest(final String... paths) {
        return target(merge(basePathRest(), paths));
    }

    /**
     * Simple GET call shortcut for server root. The path must include all required contexts: main context path and
     * rest mapping (if it's a rest call). For example, if rest mapped to "rest/*" path then path parameter must
     * include it like: "rest/my/api/smth".
     * <p>
     * This method is very basic and could be used in the simplest cases. For other cases, it could be used as
     * an example of api usage.
     *
     * @param rootPath target path, relative to server root (everything after port)
     * @param result   result type (when null, accepts any 200 or 204 responses)
     * @param <T>      result type
     * @return mapped result object or null (if class not declared)
     */
    public <T> T get(final String rootPath, final @Nullable Class<T> result) {
        if (result != null) {
            return target(basePathRoot(), rootPath).request().get(result);
        } else {
            checkVoidResponse(() -> target(basePathRoot(), rootPath).request().get());
            return null;
        }
    }

    /**
     * Simple POST call shortcut for server root. The path must include all required contexts: main context path and
     * rest mapping (if it's a rest call). For example, if rest mapped to "rest/*" path then path parameter must
     * include it like: "rest/my/api/smth".
     * <p>
     * This method is very basic and could be used in the simplest cases. For other cases, it could be used as
     * an example of api usage.
     *
     * @param rootPath target path, relative to server root (everything after port)
     * @param body     post body object (serialized as json)
     * @param result   result type (when null, accepts any 200 or 204 responses)
     * @param <T>      result type
     * @return mapped result object or null (if class not declared)
     */
    public <T> T post(final String rootPath, final @Nullable Object body, final @Nullable Class<T> result) {
        if (result != null) {
            return target(basePathRoot(), rootPath).request().post(Entity.json(body), result);
        } else {
            checkVoidResponse(() -> target(basePathRoot(), rootPath).request().post(Entity.json(body)));
            return null;
        }
    }

    /**
     * Simple PUT call shortcut for server root. The path must include all required contexts: main context path and
     * rest mapping (if it's a rest call). For example, if rest mapped to "rest/*" path then path parameter must
     * include it like: "rest/my/api/smth".
     * <p>
     * This method is very basic and could be used in the simplest cases. For other cases, it could be used as
     * an example of api usage.
     *
     * @param rootPath target path, relative to server root (everything after port)
     * @param body     post body object (serialized as json)
     * @param result   result type (when null, accepts any 200 or 204 responses)
     * @param <T>      result type
     * @return mapped result object or null (if class not declared)
     */
    public <T> T put(final String rootPath, final Object body, final @Nullable Class<T> result) {
        if (result != null) {
            return target(basePathRoot(), rootPath).request().put(Entity.json(body), result);
        } else {
            checkVoidResponse(() -> target(basePathRoot(), rootPath).request().put(Entity.json(body)));
            return null;
        }
    }

    /**
     * Simple DELETE call shortcut for server root. The path must include all required contexts: main context path and
     * rest mapping (if it's a rest call). For example, if rest mapped to "rest/*" path then path parameter must
     * include it like: "rest/my/api/smth".
     * <p>
     * This method is very basic and could be used in the simplest cases. For other cases, it could be used as
     * an example of api usage.
     *
     * @param rootPath target path, relative to server root (everything after port)
     * @param result   result type (when null, accepts any 200 or 204 responses)
     * @param <T>      result type
     * @return mapped result object or null (if class not declared)
     */
    public <T> T delete(final String rootPath, final @Nullable Class<T> result) {
        if (result != null) {
            return target(basePathRoot(), rootPath).request().delete(result);
        } else {
            checkVoidResponse(() -> target(basePathRoot(), rootPath).request().delete());
            return null;
        }
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

    /**
     * Validates response to be 200 or 204 (no content). If not, throw exception with response body.
     * <p>
     * Method is public to allow using it in custom calls.
     *
     * @param call supplier providing response
     */
    public void checkVoidResponse(final Supplier<Response> call) {
        try (Response res = call.get()) {
            if (!Arrays.asList(HttpStatus.OK_200, HttpStatus.NO_CONTENT_204).contains(res.getStatus())) {
                throw new IllegalStateException("Invalid response: " + res.getStatus() + "\n"
                        + res.readEntity(String.class));
            }
        }
    }

    private String[] merge(final String base, final String... addition) {
        final String[] res;
        if (addition.length == 0) {
            res = new String[]{base};
        } else {
            res = new String[addition.length + 1];
            res[0] = base;
            System.arraycopy(addition, 0, res, 1, addition.length);
        }
        return res;
    }
}
