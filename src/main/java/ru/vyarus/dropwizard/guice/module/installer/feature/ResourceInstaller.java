package ru.vyarus.dropwizard.guice.module.installer.feature;


import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils;

import javax.ws.rs.Path;

/**
 * Jersey resource installer.
 * Search classes annotated with {@code @Path}. Installers mechanism will register bean in context and
 * jersey-guice will handle registration into dropwizard jersey context (no need for special installation logic)
 * <p>NOTE: if you will nor annotate resource with @Singleton, it will be used as prototype and
 * instantiated on each request!</p>
 */
public class ResourceInstaller implements FeatureInstaller<Object> {

    @Override
    public boolean matches(final Class<?> type) {
        return FeatureUtils.hasAnnotation(type, Path.class);
    }

    @Override
    public void install(final Environment environment, final Object instance) {
        environment.jersey().register(instance);
        // no need for installation, instance creation is enough for jersey-guice to register
    }
}
