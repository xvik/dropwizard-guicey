package ru.vyarus.dropwizard.guice.test.spock.ext;

import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension;
import org.spockframework.runtime.model.SpecInfo;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyConfiguration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * {@link UseGuiceyConfiguration} extension implementation.
 *
 * @author Vyacheslav Rusakov
 * @since 12.04.2018
 */
public class GuiceyConfigurationExtension extends AbstractAnnotationDrivenExtension<UseGuiceyConfiguration> {

    private Class<? extends GuiceyConfigurationHook>[] confs;

    @Override
    public void visitSpecAnnotation(final UseGuiceyConfiguration annotation, final SpecInfo spec) {
        confs = annotation.value();
    }

    @Override
    public void visitSpec(final SpecInfo spec) {
        final GuiceyConfigurationHookInterceptor interceptor =
                new GuiceyConfigurationHookInterceptor(instantiate(confs));
        // hook interceptor MUST go first, otherwise guicey will start without these changes
        spec.getTopSpec().getSharedInitializerInterceptors().add(0, interceptor);
        spec.getTopSpec().addCleanupSpecInterceptor(interceptor);
    }

    @SafeVarargs
    public static final List<GuiceyConfigurationHook> instantiate(
            final Class<? extends GuiceyConfigurationHook>... confs) {
        return Arrays.stream(confs).map(conf -> {
            try {
                return conf.newInstance();
            } catch (Exception e) {
                throw new IllegalStateException("Failed to instantiate guicey hook: "
                        + conf.getSimpleName(), e);
            }
        }).collect(Collectors.toList());
    }
}
