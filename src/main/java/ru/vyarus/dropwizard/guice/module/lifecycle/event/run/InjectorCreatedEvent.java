package ru.vyarus.dropwizard.guice.module.lifecycle.event.run;

import com.google.inject.Injector;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.module.context.option.OptionsInfo;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.InjectorPhaseEvent;

/**
 * Called after injector creation. Note that starting from this event you have access to injector object.
 * Extensions are not yer installed at this point!
 * <p>
 * Public guicey configuration api {@link #getConfigurationInfo()} could be accessed starting at this point.
 * Also, configuration reports could be build with {@link #getReportRenderer()}.
 *
 * @author Vyacheslav Rusakov
 * @since 19.04.2018
 */
public class InjectorCreatedEvent extends InjectorPhaseEvent {

    public InjectorCreatedEvent(final OptionsInfo options,
                                final Bootstrap bootstrap,
                                final Configuration configuration,
                                final Environment environment,
                                final Injector injector) {
        super(GuiceyLifecycle.InjectorCreated, options, bootstrap, configuration, environment, injector);
    }
}
