package ru.vyarus.dropwizard.guice.test.spock.ext;

import io.dropwizard.testing.ConfigOverride;
import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension;
import org.spockframework.runtime.model.SpecInfo;
import ru.vyarus.dropwizard.guice.module.support.conf.GuiceyConfigurator;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Base class for guicey spock extensions. Extensions use junit rules inside to avoid duplication.
 *
 * @param <T> extension annotation
 * @author Vyacheslav Rusakov
 * @since 03.01.2015
 */
public abstract class AbstractAppExtension<T extends Annotation> extends AbstractAnnotationDrivenExtension<T> {

    private T annotation;

    @Override
    public void visitSpecAnnotation(final T useApplication, final SpecInfo spec) {
        this.annotation = useApplication;
    }

    @Override
    public void visitSpec(final SpecInfo spec) {
        final List<GuiceyConfigurator> configurators =
                GuiceyConfiguratorExtension.instantiate(getConfigurators(annotation));
        final GuiceyInterceptor interceptor =
                new GuiceyInterceptor(spec, buildResourceFactory(annotation), configurators);
        final SpecInfo topSpec = spec.getTopSpec();
        topSpec.addSharedInitializerInterceptor(interceptor);
        topSpec.addInitializerInterceptor(interceptor);
        topSpec.addCleanupSpecInterceptor(interceptor);
    }

    /**
     * @param annotation extension annotation instance
     * @return configurator classes defined in annotation
     */
    protected abstract Class<? extends GuiceyConfigurator>[] getConfigurators(T annotation);

    /**
     * @param annotation extension annotation instance
     * @return resource factory instance, which will create correct rule instance
     */
    protected abstract GuiceyInterceptor.ExternalRuleAdapter buildResourceFactory(T annotation);

    /**
     * Utility method to convert configuration overrides from annotation to rule compatible format.
     *
     * @param overrides override annotations
     * @return dropwizard config override objects
     */
    protected ConfigOverride[] convertOverrides(
            final ru.vyarus.dropwizard.guice.test.spock.ConfigOverride... overrides) {
        final ConfigOverride[] configOverride = new ConfigOverride[overrides.length];
        int i = 0;
        for (ru.vyarus.dropwizard.guice.test.spock.ConfigOverride override : overrides) {
            configOverride[i++] = ConfigOverride.config(override.key(), override.value());
        }
        return configOverride;
    }
}
