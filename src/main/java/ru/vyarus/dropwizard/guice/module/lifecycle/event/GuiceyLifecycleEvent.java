package ru.vyarus.dropwizard.guice.module.lifecycle.event;

import com.google.common.base.Preconditions;
import ru.vyarus.dropwizard.guice.module.context.option.Options;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;

/**
 * Base class for guicey lifecycle events. All events are organized in hierarchy by:
 * <ul>
 * <li>{@link GuiceyLifecycleEvent} - lowest level, provides access to options (ideally there should be
 * bootstrap object, which is known for all events, but first event
 * {@link ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration.ConfigurationHooksProcessedEvent}) simply
 * don't have access for it. So bootstrap is only provided in
 * {@link ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration.InitializationEvent}</li>
 * <li>{@link RunPhaseEvent} - all other events (started in bundle run phase and even after it)</li>
 * <li>{@link InjectorPhaseEvent} - all events after guice injector creation</li>
 * <li>{@link HK2PhaseEvent} - all events after hk context initialization start (since
 * {@link org.glassfish.hk2.api.ServiceLocator} become available)</li>
 * </ul>
 *
 * @author Vyacheslav Rusakov
 * @since 19.04.2018
 */
public abstract class GuiceyLifecycleEvent {

    private final GuiceyLifecycle type;
    private final Options options;


    public GuiceyLifecycleEvent(final GuiceyLifecycle type, final Options options) {
        Preconditions.checkState(type.getType().equals(getClass()),
                "Wrong event type %s used for class %s", type, getClass().getSimpleName());
        this.type = type;
        this.options = options;
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
}
