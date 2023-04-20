package ru.vyarus.dropwizard.guice.module.lifecycle.event;

import com.google.common.base.Preconditions;
import ru.vyarus.dropwizard.guice.module.context.SharedConfigurationState;
import ru.vyarus.dropwizard.guice.module.context.option.Options;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.internal.EventsContext;

/**
 * Base class for guicey lifecycle events. All events are organized in hierarchy by:
 * <ul>
 * <li>{@link GuiceyLifecycleEvent} - lowest level, provides access to options (ideally there should be
 * bootstrap object, which is known for all events, but first event
 * {@link ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration.ConfigurationHooksProcessedEvent}) simply
 * don't have access for it.
 * <li>{@link ConfigurationPhaseEvent} - dropwizard configuration phase events (all have access to dropwizard
 * {@link io.dropwizard.setup.Bootstrap} object)</li>
 * <li>{@link RunPhaseEvent} - events started on dropwizard run phase (when configuration is available)</li>
 * <li>{@link InjectorPhaseEvent} - all events after guice injector creation</li>
 * <li>{@link JerseyPhaseEvent} - all events after jersey context initialization start (since
 * {@link org.glassfish.jersey.internal.inject.InjectionManager} become available)</li>
 * </ul>
 *
 * @author Vyacheslav Rusakov
 * @since 19.04.2018
 */
public abstract class GuiceyLifecycleEvent {

    private final GuiceyLifecycle type;
    private final Options options;
    private final SharedConfigurationState sharedState;


    public GuiceyLifecycleEvent(final GuiceyLifecycle type,
                                final EventsContext context) {
        Preconditions.checkState(type.getType().equals(getClass()),
                "Wrong event type %s used for class %s", type, getClass().getSimpleName());
        this.type = type;
        this.options = context.getOptions();
        this.sharedState = context.getSharedState();
    }

    /**
     * Useful to differentiate events (with switch).
     *
     * @return type of event
     */
    public GuiceyLifecycle getType() {
        return type;
    }

    /**
     * NOTE: options could only be specified in application class (in bundle definition) and so for all events
     * options will be the same (already defined).
     *
     * @return defined options
     */
    public Options getOptions() {
        return options;
    }

    /**
     * @return application shared state
     * @see SharedConfigurationState
     */
    public SharedConfigurationState getSharedState() {
        return sharedState;
    }
}
