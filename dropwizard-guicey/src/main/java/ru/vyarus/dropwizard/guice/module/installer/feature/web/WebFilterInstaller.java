package ru.vyarus.dropwizard.guice.module.installer.feature.web;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import io.dropwizard.jetty.setup.ServletEnvironment;
import io.dropwizard.core.setup.Environment;
import ru.vyarus.dropwizard.guice.debug.util.RenderUtils;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.feature.web.util.WebUtils;
import ru.vyarus.dropwizard.guice.module.installer.install.InstanceInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.WebInstaller;
import ru.vyarus.dropwizard.guice.module.installer.order.Order;
import ru.vyarus.dropwizard.guice.module.installer.order.Ordered;
import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils;
import ru.vyarus.dropwizard.guice.module.installer.util.Reporter;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.annotation.WebInitParam;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

/**
 * Search for http filters annotated with {@link WebFilter} (servlet api annotation). Such filters will not
 * be installed by jetty because dropwizard didn't depend on jetty-annotations.
 * <p>
 * Only the following {@link WebFilter} annotation properties are supported: filterName, urlPatterns ( or value),
 * servletNames, dispatcherTypes, initParams, asyncSupported. Url patterns and servlet names can't be used at the
 * same time.
 * <p>
 * When filter name not defined, then name will be generated as: . (dot) at the beginning to indicate
 * generated name, followed by lower-cased class name. If class ends with "filter" then it will be cut off.
 * For example, for class "MyCoolFilter" generated name will be ".mycool".
 * <p>
 * By default, everything is installed for main context. Special annotation
 * {@link ru.vyarus.dropwizard.guice.module.installer.feature.web.AdminContext} must be used to install into admin
 * or both contexts.
 * <p>
 * Reporting format: <pre>[urls or servlets mapping] [context markers: M - main, A - admin] (class) [filter name]</pre>.
 * If filter registered only in main context, then context marker (M) is not shown.
 *
 * @author Vyacheslav Rusakov
 * @since 06.08.2016
 */
@Order(100)
public class WebFilterInstaller implements FeatureInstaller,
        InstanceInstaller<Filter>, Ordered, WebInstaller {

    private final Reporter reporter = new Reporter(WebFilterInstaller.class, "filters =");

    @Override
    public boolean matches(final Class<?> type) {
        return FeatureUtils.is(type, Filter.class)
                && FeatureUtils.hasAnnotation(type, WebFilter.class);
    }

    @Override
    public void install(final Environment environment, final Filter instance) {
        final Class<? extends Filter> extType = FeatureUtils.getInstanceClass(instance);
        final WebFilter annotation = FeatureUtils.getAnnotation(extType, WebFilter.class);
        final String[] servlets = annotation.servletNames();
        final String[] patterns = annotation.urlPatterns().length > 0 ? annotation.urlPatterns() : annotation.value();
        Preconditions.checkArgument(servlets.length > 0 || patterns.length > 0,
                "Filter %s not specified servlet or pattern for mapping", extType.getName());
        Preconditions.checkArgument(servlets.length == 0 || patterns.length == 0,
                "Filter %s specifies both servlets and patters, when only one allowed",
                extType.getName());
        final boolean servletMapping = servlets.length > 0;
        final AdminContext context = FeatureUtils.getAnnotation(extType, AdminContext.class);
        final String name = WebUtils.getFilterName(annotation, extType);
        reporter.line("%-25s %-8s %-4s %s   %s", Joiner.on(",").join(servletMapping ? servlets : patterns),
                WebUtils.getAsyncMarker(annotation), WebUtils.getContextMarkers(context),
                RenderUtils.renderClassLine(extType), name);

        if (WebUtils.isForMain(context)) {
            configure(environment.servlets(), instance, name, annotation);
        }
        if (WebUtils.isForAdmin(context)) {
            configure(environment.admin(), instance, name, annotation);
        }
    }

    @Override
    public void report() {
        reporter.report();
    }

    @SuppressWarnings("PMD.LooseCoupling")
    private void configure(final ServletEnvironment environment, final Filter filter,
                           final String name, final WebFilter annotation) {
        final FilterRegistration.Dynamic mapping = environment.addFilter(name, filter);
        final EnumSet<DispatcherType> dispatcherTypes = EnumSet.copyOf(Arrays.asList(annotation.dispatcherTypes()));
        if (annotation.servletNames().length > 0) {
            mapping.addMappingForServletNames(dispatcherTypes, false, annotation.servletNames());
        } else {
            final String[] urlPatterns = annotation.urlPatterns().length > 0
                    ? annotation.urlPatterns() : annotation.value();
            mapping.addMappingForUrlPatterns(dispatcherTypes, false, urlPatterns);
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
        return Collections.singletonList("implements " + Filter.class.getSimpleName()
                + " + @" + WebFilter.class.getSimpleName());
    }
}
