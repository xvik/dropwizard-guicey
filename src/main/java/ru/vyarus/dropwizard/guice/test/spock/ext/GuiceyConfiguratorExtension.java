package ru.vyarus.dropwizard.guice.test.spock.ext;

import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension;
import org.spockframework.runtime.model.SpecInfo;
import ru.vyarus.dropwizard.guice.configurator.GuiceyConfigurator;
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyConfigurator;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * {@link UseGuiceyConfigurator} extension implementation.
 *
 * @author Vyacheslav Rusakov
 * @since 12.04.2018
 */
public class GuiceyConfiguratorExtension extends AbstractAnnotationDrivenExtension<UseGuiceyConfigurator> {

    private Class<? extends GuiceyConfigurator>[] confs;

    @Override
    public void visitSpecAnnotation(final UseGuiceyConfigurator annotation, final SpecInfo spec) {
        confs = annotation.value();
    }

    @Override
    public void visitSpec(final SpecInfo spec) {
        final GuiceyConfiguratorInterceptor interceptor =
                new GuiceyConfiguratorInterceptor(instantiate(confs));
        // configurator interceptor MUST go first, otherwise guicey will start without these changes
        spec.getTopSpec().getSharedInitializerInterceptors().add(0, interceptor);
        spec.getTopSpec().addCleanupSpecInterceptor(interceptor);
    }

    @SafeVarargs
    public static final List<GuiceyConfigurator> instantiate(final Class<? extends GuiceyConfigurator>... confs) {
        return Arrays.stream(confs).map(conf -> {
            try {
                return conf.newInstance();
            } catch (Exception e) {
                throw new IllegalStateException("Failed to instantiate guicey configurator: "
                        + conf.getSimpleName(), e);
            }
        }).collect(Collectors.toList());
    }
}
