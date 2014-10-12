package ru.vyarus.dropwizard.guice.module.jersey;

import com.google.common.base.Function;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.sun.jersey.spi.container.servlet.WebConfig;
import io.dropwizard.setup.Environment;

import javax.annotation.Nullable;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import java.util.Map;

/**
 * Jersey integration: adopted guice container.
 * Based on source from <a href="https://github.com/HubSpot/dropwizard-guice">dropwizard-guice</a>.
 *
 * @author eliast
 * @author Vyacheslav Rusakov
 * @since 31.08.2014
 */
@Singleton
public class DropwizardGuiceContainer extends GuiceContainer {

    private ResourceConfig resourceConfig;

    @Inject
    public DropwizardGuiceContainer(final Injector injector, final Environment environment) {
        super(injector);
        bindEnvironment(environment);
    }

    private void bindEnvironment(final Environment environment) {
        resourceConfig = environment.jersey().getResourceConfig();
        environment.jersey().replace(new Function<ResourceConfig, ServletContainer>() {
            @Nullable
            @Override
            public ServletContainer apply(final ResourceConfig resourceConfig) {
                return DropwizardGuiceContainer.this;
            }
        });
        environment.servlets().addFilter("Guice Filter", GuiceFilter.class)
                .addMappingForUrlPatterns(null, false, environment.getApplicationContext().getContextPath() + "*");
    }

    @Override
    protected ResourceConfig getDefaultResourceConfig(
            final Map<String, Object> props,
            final WebConfig webConfig) throws ServletException {
        return resourceConfig;
    }
}
