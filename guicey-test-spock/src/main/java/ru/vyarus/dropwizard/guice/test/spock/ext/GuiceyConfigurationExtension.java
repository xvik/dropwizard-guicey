package ru.vyarus.dropwizard.guice.test.spock.ext;

import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension;
import org.spockframework.runtime.model.SpecInfo;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyHooks;
import ru.vyarus.dropwizard.guice.test.util.HooksUtil;

/**
 * {@link UseGuiceyHooks} extension implementation.
 *
 * @author Vyacheslav Rusakov
 * @since 12.04.2018
 * @deprecated additional hooks may be declared in static test fields
 */
@Deprecated
public class GuiceyConfigurationExtension extends AbstractAnnotationDrivenExtension<UseGuiceyHooks> {

    private Class<? extends GuiceyConfigurationHook>[] confs;

    @Override
    public void visitSpecAnnotation(final UseGuiceyHooks annotation, final SpecInfo spec) {
        confs = annotation.value();
    }

    @Override
    public void visitSpec(final SpecInfo spec) {
        final GuiceyConfigurationHookInterceptor interceptor =
                new GuiceyConfigurationHookInterceptor(HooksUtil.create(confs));
        // hook interceptor MUST go first, otherwise guicey will start without these changes
        spec.getTopSpec().getSharedInitializerInterceptors().add(0, interceptor);
        spec.getTopSpec().addCleanupSpecInterceptor(interceptor);
    }
}
