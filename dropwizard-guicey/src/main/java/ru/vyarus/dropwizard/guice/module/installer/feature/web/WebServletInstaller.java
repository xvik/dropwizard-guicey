package ru.vyarus.dropwizard.guice.module.installer.feature.web;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import io.dropwizard.jetty.setup.ServletEnvironment;
import io.dropwizard.core.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.debug.util.RenderUtils;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.feature.web.util.WebUtils;
import ru.vyarus.dropwizard.guice.module.installer.install.InstanceInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.WebInstaller;
import ru.vyarus.dropwizard.guice.module.installer.option.InstallerOptionsSupport;
import ru.vyarus.dropwizard.guice.module.installer.order.Order;
import ru.vyarus.dropwizard.guice.module.installer.order.Ordered;
import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils;
import ru.vyarus.dropwizard.guice.module.installer.util.Reporter;

import jakarta.servlet.ServletRegistration;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static ru.vyarus.dropwizard.guice.module.installer.InstallersOptions.DenyServletRegistrationWithClash;

/**
 * Search for http servlets annotated with {@link WebServlet} (servlet api annotation). Such servlets will not
 * be installed by jetty because dropwizard didn't depend on jetty-annotations.
 * <p>
 * Only the following {@link WebServlet} annotation properties are supported: name, urlPatterns ( or value),
 * initParams, asyncSupported.
 * <p>
 * When servlet name not defined, then name will be generated as: . (dot) at the beginning to indicate
 * generated name, followed by lower-cased class name. If class ends with "servlet" then it will be cut off.
 * For example, for class "MyCoolServlet" generated name will be ".mycool".
 * <p>
 * If servlet mapping clash (partially or completely) with some other servlet then warning log will be printed,
 * but overall process will not fail. Use
 * {@link ru.vyarus.dropwizard.guice.module.installer.InstallersOptions#DenyServletRegistrationWithClash} to throw
 * exception instead of warning.
 * <p>
 * By default, everything is installed for main context. Special annotation
 * {@link ru.vyarus.dropwizard.guice.module.installer.feature.web.AdminContext} must be used to install into admin
 * or both contexts.
 * <p>
 * Reporting format: <pre>[urls] [context markers: M - main, A - admin] (class) [servlet name]</pre>.
 * If servlet registered only in main context, then context marker (M) is not shown.
 *
 * @author Vyacheslav Rusakov
 * @since 06.08.2016
 */
@Order(90)
public class WebServletInstaller extends InstallerOptionsSupport
        implements FeatureInstaller, InstanceInstaller<HttpServlet>, Ordered, WebInstaller {

    private final Logger logger = LoggerFactory.getLogger(WebServletInstaller.class);
    private final Reporter reporter = new Reporter(WebServletInstaller.class, "servlets =");

    @Override
    public boolean matches(final Class<?> type) {
        return FeatureUtils.is(type, HttpServlet.class)
                && FeatureUtils.hasAnnotation(type, WebServlet.class);
    }

    @Override
    public void install(final Environment environment, final HttpServlet instance) {
        final Class<? extends HttpServlet> extType = FeatureUtils.getInstanceClass(instance);
        final WebServlet annotation = FeatureUtils.getAnnotation(extType, WebServlet.class);
        final String[] patterns = annotation.urlPatterns().length > 0 ? annotation.urlPatterns() : annotation.value();
        Preconditions.checkArgument(patterns.length > 0,
                "Servlet %s not specified url pattern for mapping", extType.getName());
        final AdminContext context = FeatureUtils.getAnnotation(extType, AdminContext.class);
        final String name = WebUtils.getServletName(annotation, extType);
        reporter.line("%-25s %-8s %-4s %s   %s", Joiner.on(",").join(patterns),
                WebUtils.getAsyncMarker(annotation), WebUtils.getContextMarkers(context),
                RenderUtils.renderClassLine(extType), name);

        if (WebUtils.isForMain(context)) {
            configure(environment.servlets(), instance, extType, name, annotation);
        }
        if (WebUtils.isForAdmin(context)) {
            configure(environment.admin(), instance, extType, name, annotation);
        }
    }

    @Override
    public void report() {
        reporter.report();
    }

    private void configure(final ServletEnvironment environment, final HttpServlet servlet,
                           final Class<? extends HttpServlet> type, final String name, final WebServlet annotation) {
        final ServletRegistration.Dynamic mapping = environment.addServlet(name, servlet);
        final Set<String> clash = mapping
                .addMapping(annotation.urlPatterns().length > 0 ? annotation.urlPatterns() : annotation.value());
        if (clash != null && !clash.isEmpty()) {
            final String msg = String.format(
                    "Servlet registration %s clash with already installed servlets on paths: %s",
                    type.getSimpleName(), Joiner.on(',').join(clash));
            if (option(DenyServletRegistrationWithClash)) {
                throw new IllegalStateException(msg);
            } else {
                logger.warn(msg);
            }
        }
        if (annotation.initParams().length > 0) {
            for (WebInitParam param : annotation.initParams()) {
                mapping.setInitParameter(param.name(), param.value());
            }
        }
        mapping.setAsyncSupported(annotation.asyncSupported());
    }

    @Override
    public List<String> getRecognizableSigns() {
        return Collections.singletonList("extends " + HttpServlet.class.getSimpleName()
                + " + @" + WebServlet.class.getSimpleName());
    }
}
