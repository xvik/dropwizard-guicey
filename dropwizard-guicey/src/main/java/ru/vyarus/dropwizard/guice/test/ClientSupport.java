package ru.vyarus.dropwizard.guice.test;

import com.google.common.base.Preconditions;
import io.dropwizard.jersey.jackson.JacksonFeature;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.testing.DropwizardTestSupport;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import ru.vyarus.dropwizard.guice.module.installer.util.PathUtils;

import javax.ws.rs.client.WebTarget;

/**
 * {@link JerseyClient} support for direct web tests (complete dropwizard startup).
 * <p>
 * Client support maintains single {@link JerseyClient} instance. It may be used for calling any urls (not just
 * application). Class provides many utility methods for automatic construction of base context paths, so
 * tests could be completely independent from actual configuration.
 *
 * @author Vyacheslav Rusakov
 * @since 04.05.2020
 */
public class ClientSupport implements AutoCloseable {
    private static final String HTTP_LOCALHOST = "http://localhost:";

    private final DropwizardTestSupport<?> support;
    private JerseyClient client;

    public ClientSupport(final DropwizardTestSupport<?> support) {
        this.support = support;
    }

    /**
     * Single client instance maintained within test and method will always return the same instance.
     *
     * @return client instance
     */
    public JerseyClient getClient() {
        synchronized (this) {
            if (client == null) {
                client = clientBuilder().build();
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
                PathUtils.path(HTTP_LOCALHOST + getPort(), contextMapping));
    }

    /**
     * For example, with default configuration it would be "http://localhost:8080/". If "server.rootPath"
     * would be changed to "/someth" then method will return "http://localhost:8080/someth/".
     * If main context mapping changed from root, then returned path will count in too
     * (e.g. "http://localhost:8080/root/rest/", when "server.applicationContextPath" is "/root").
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
     * For example, with default configuration it would be "http://localhost:8080/". If "server.applicationContextPath"
     * would be changed to "/someth" then method will return "http://localhost:8080/someth/".
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
                PathUtils.path(HTTP_LOCALHOST + getPort(), contextPath, restMapping));
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
     * @see #basePathMain() for base use construction details
     * @throws NullPointerException for guicey test
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
     * @see #basePathAdmin() for base use construction details
     * @throws NullPointerException for guicey test
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
     * @see #basePathRest() for base use construction details
     * @throws NullPointerException for guicey test
     */
    public WebTarget targetRest(final String... paths) {
        return target(merge(basePathRest(), paths));
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

    private JerseyClientBuilder clientBuilder() {
        return new JerseyClientBuilder()
                .register(new JacksonFeature(support.getEnvironment().getObjectMapper()))
                .property(ClientProperties.CONNECT_TIMEOUT, 1000)
                .property(ClientProperties.READ_TIMEOUT, 5000)
                .property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true);
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
