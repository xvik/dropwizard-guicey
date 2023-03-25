package ru.vyarus.guicey.eventbus.module;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matcher;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import ru.vyarus.guicey.eventbus.service.EventSubscribersInfo;

import javax.inject.Singleton;

/**
 * Module binds provided {@link EventBus} instance. Publishers should inject event bus for posting events.
 * Listeners must only define method with event as argument and annotated with {@link Subscribe}. All guice beans
 * with annotated methods registered automatically.
 *
 * @author Vyacheslav Rusakov
 * @see EventSubscribersInfo guice bean registered for programmatic subscribers info access
 * @since 12.10.2016
 */
public class EventBusModule extends AbstractModule {

    private final EventBus eventbus;
    private final Matcher<? super TypeLiteral<?>> typeMatcher;

    public EventBusModule(final EventBus eventbus,
                          final Matcher<? super TypeLiteral<?>> typeMatcher) {
        this.eventbus = eventbus;
        this.typeMatcher = typeMatcher;
    }

    @Override
    protected void configure() {
        bind(EventBus.class).toInstance(eventbus);
        bind(EventSubscribersInfo.class).in(Singleton.class);

        bindListener();
    }

    @SuppressWarnings("unchecked")
    private void bindListener() {
        bindListener(typeMatcher, new TypeListener() {
            @Override
            public <I> void hear(final TypeLiteral<I> type, final TypeEncounter<I> encounter) {
                // register all beans: event bus will introspect each class and register found listeners
                // duplicate registrations are valid (internal event bus cache will handle it)
                encounter.register((InjectionListener) eventbus::register);
            }
        });
    }
}
