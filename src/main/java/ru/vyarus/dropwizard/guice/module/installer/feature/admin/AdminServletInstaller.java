package ru.vyarus.dropwizard.guice.module.installer.feature.admin;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.InstanceInstaller;
import ru.vyarus.dropwizard.guice.module.installer.order.Order;
import ru.vyarus.dropwizard.guice.module.installer.order.Ordered;
import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils;
import ru.vyarus.dropwizard.guice.module.installer.util.Reporter;

import javax.servlet.http.HttpServlet;

/**
 * Search for servlets annotated with {@link AdminServlet} and installs into admin context.
 *
 * @author Vyacheslav Rusakov
 * @since 13.10.2014
 */
@Order(100)
public class AdminServletInstaller implements FeatureInstaller<HttpServlet>,
        InstanceInstaller<HttpServlet>, Ordered {

    private final Reporter reporter = new Reporter(AdminServletInstaller.class, "admin servlets =");

    @Override
    public boolean matches(final Class<?> type) {
        return FeatureUtils.is(type, HttpServlet.class)
                && FeatureUtils.hasAnnotation(type, AdminServlet.class);
    }

    @Override
    public void install(final Environment environment, final HttpServlet instance) {
        final Class<? extends HttpServlet> extType = FeatureUtils.getInstanceClass(instance);
        final AdminServlet annotation = FeatureUtils.getAnnotation(extType, AdminServlet.class);
        final String servletName = Preconditions.checkNotNull(Strings.emptyToNull(annotation.name()),
                "Servlet name not specified for servlet %s", extType.getName());
        final String[] patterns = annotation.patterns();
        reporter.line("%-10s %-10s (%s)", servletName, Joiner.on(",").join(patterns),
                extType.getName());
        environment.admin().addServlet(servletName, instance).addMapping(patterns);
    }

    @Override
    public void report() {
        reporter.report();
    }
}
