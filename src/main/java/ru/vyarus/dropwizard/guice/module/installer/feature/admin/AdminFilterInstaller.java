package ru.vyarus.dropwizard.guice.module.installer.feature.admin;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.InstanceInstaller;
import ru.vyarus.dropwizard.guice.module.installer.order.Ordered;
import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils;
import ru.vyarus.dropwizard.guice.module.installer.util.Reporter;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;

/**
 * Search for filters annotated with {@link AdminFilter} and installs into admin context.
 *
 * @author Vyacheslav Rusakov
 * @since 13.10.2014
 */
public class AdminFilterInstaller implements FeatureInstaller<Filter>,
        InstanceInstaller<Filter>, Ordered {

    private final Reporter reporter = new Reporter(AdminServletInstaller.class, "admin filters =");

    @Override
    public boolean matches(final Class<?> type) {
        return FeatureUtils.is(type, Filter.class)
                && FeatureUtils.hasAnnotation(type, AdminFilter.class);
    }

    @Override
    public void install(final Environment environment, final Filter instance) {
        final AdminFilter annotation = FeatureUtils.getAnnotation(instance.getClass(), AdminFilter.class);
        final String[] servlets = annotation.servlets();
        final String[] patterns = annotation.patterns();
        final String filterName = Preconditions.checkNotNull(Strings.emptyToNull(annotation.name()),
                "Filter name not specified for servlet %s", instance.getClass().getName());
        Preconditions.checkArgument(servlets.length > 0 || patterns.length > 0,
                "Filter %s not specified servlet or pattern for mapping", instance.getClass().getName());
        Preconditions.checkArgument(servlets.length == 0 || patterns.length == 0,
                "Filter %s specifies both servlets and patters, when only one allowed",
                instance.getClass().getName());
        final boolean servletMapping = servlets.length > 0;
        reporter.line("%-10s %-10s (%s)",
                filterName,
                Joiner.on(",").join(servletMapping ? servlets : patterns),
                instance.getClass().getName());
        final FilterRegistration.Dynamic mapping = environment.admin().addFilter(filterName, instance);
        if (servletMapping) {
            mapping.addMappingForServletNames(null, false, servlets);
        } else {
            mapping.addMappingForUrlPatterns(null, false, patterns);
        }
    }

    @Override
    public void report() {
        reporter.report();
    }
}
