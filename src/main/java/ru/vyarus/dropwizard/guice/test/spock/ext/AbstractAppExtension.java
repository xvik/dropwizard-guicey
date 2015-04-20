package ru.vyarus.dropwizard.guice.test.spock.ext;

import io.dropwizard.testing.ConfigOverride;
import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension;
import org.spockframework.runtime.model.SpecInfo;

import java.lang.annotation.Annotation;

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
        final GuiceyInterceptor interceptor = new GuiceyInterceptor(spec, buildResourceFactory(annotation));
        final SpecInfo topSpec = spec.getTopSpec();
        topSpec.addSharedInitializerInterceptor(interceptor);
        topSpec.addInitializerInterceptor(interceptor);
        topSpec.addCleanupSpecInterceptor(interceptor);
    }

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
