package ru.vyarus.guicey.admin;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.core.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.module.context.unique.item.UniqueGuiceyBundle;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyEnvironment;
import ru.vyarus.guicey.admin.rest.AdminResourceFilter;
import ru.vyarus.guicey.admin.rest.AdminRestServlet;

/**
 * Adds rest support in admin context by simply redirecting from admin servlet into jersey (all rest methods
 * are accessible from admin context).
 * <p>
 * Such approach is better than registering completely separate jersey context for admin rest because
 * of no overhead and simplicity of jersey extensions management.
 * <p>
 * If no specific mapping path specified, rest will be mapped into the same path as main rest.
 * If main rest mapping default ('/*') isn't changed, admin rest will be mapped to '/api/*'.
 * <p>
 * In order to hide admin specific rest methods or entire resources
 * {@link ru.vyarus.guicey.admin.rest.AdminResource} annotation may be used.
 * If some security solution is used within application, rest could be hidden with security framework permissions.
 *
 * @author Vyacheslav Rusakov
 * @since 05.08.2015
 */
public class AdminRestBundle extends UniqueGuiceyBundle {
    private static final String ROOT_PATH = "/*";
    private final Logger logger = LoggerFactory.getLogger(AdminRestBundle.class);

    private final String path;

    /**
     * Admin rest will be mapped on the same path as main rest if rest mapping is different from '/*'.
     * Otherwise admin rest mapped to '/api/*'.
     */
    public AdminRestBundle() {
        this(null);
    }

    /**
     * Path must end with '/*', otherwise error will be thrown.
     * For example, '/rest/*' is a valid path.
     *
     * @param path path to map admin rest on
     */
    public AdminRestBundle(final String path) {
        this.path = path;
    }

    @Override
    public void run(final GuiceyEnvironment environment) throws Exception {
        environment.manage(new ServletRegistration(environment.environment()));
    }

    private void registerServlet(final String path, final Environment environment) {
        environment.admin()
                .addServlet("adminRest", new AdminRestServlet(environment.getJerseyServletContainer()))
                .addMapping(path);
        environment.jersey().register(AdminResourceFilter.class);
        logger.info("Admin REST registered on path: {}", path);
    }

    /**
     * Managed object is required  because rest mapping from configuration (servlet.rootPath)
     * is available only on managed start phase.
     */
    private class ServletRegistration implements Managed {
        private final Environment environment;

        ServletRegistration(final Environment environment) {
            this.environment = environment;
        }

        @Override
        public void start() throws Exception {
            String restPath = path;
            if (Strings.isNullOrEmpty(restPath)) {
                restPath = environment.jersey().getUrlPattern();
                // we can't map rest to the root of admin context
                if (ROOT_PATH.equals(restPath)) {
                    restPath = "/api/*";
                }
            }
            Preconditions.checkState(restPath.endsWith(ROOT_PATH),
                    "Rest path must end with '/*', but configured one is not: %s", restPath);
            registerServlet(restPath, environment);
        }

        @Override
        public void stop() throws Exception {
            // not needed
        }
    }
}
