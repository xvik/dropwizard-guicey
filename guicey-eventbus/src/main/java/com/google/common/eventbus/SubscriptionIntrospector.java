package com.google.common.eventbus;

import com.google.common.base.Preconditions;
import ru.vyarus.guicey.eventbus.service.EventSubscribersInfo;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Provides access for registered event subscribers.
 *
 * @author Vyacheslav Rusakov
 * @see EventSubscribersInfo for usage
 * @since 02.12.2016
 */
public class SubscriptionIntrospector {

    private static final String SUBSCRIBERS_FIELD = "subscribers";

    private final EventBus eventbus;
    private Map<Class, Set<Subscriber>> subscribers;

    /**
     * Create an introspector.
     *
     * @param eventbus event bus instance
     */
    public SubscriptionIntrospector(final EventBus eventbus) {
        this.eventbus = eventbus;
    }

    /**
     * @return event classes
     */
    public Set<Class> getListenedEvents() {
        return extractSubscribers().keySet();
    }

    /**
     * @param event event class
     * @return event subscribers
     */
    public Set<Object> getSubscribers(final Class event) {
        final Set<Object> res = new HashSet<>();
        final Set<Subscriber> subscribers = extractSubscribers().get(event);
        if (subscribers != null) {
            for (Subscriber subs : subscribers) {
                res.add(subs.target);
            }
        }
        return res;
    }

    /**
     * @param event event class
     * @return subscriber classes
     * @see #getSubscribers(Class) for instances
     */
    public Set<Class> getSubscriberTypes(final Class event) {
        final Set<Class> res = new HashSet<>();
        for (Object obj : getSubscribers(event)) {
            res.add(extractType(obj));
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    private Map<Class, Set<Subscriber>> extractSubscribers() {
        synchronized (this) {
            if (subscribers == null) {
                try {
                    final Field registryField = EventBus.class.getDeclaredField(SUBSCRIBERS_FIELD);
                    registryField.setAccessible(true);
                    final SubscriberRegistry registry = (SubscriberRegistry) registryField.get(eventbus);

                    final Field subscribersField = SubscriberRegistry.class.getDeclaredField(SUBSCRIBERS_FIELD);
                    subscribersField.setAccessible(true);

                    subscribers = (Map<Class, Set<Subscriber>>) Preconditions
                            .checkNotNull(subscribersField.get(registry));
                } catch (Exception e) {
                    throw new IllegalStateException("Failed to access subscribers collection", e);
                }
            }
            return subscribers;
        }
    }

    private Class extractType(final Object instance) {
        Class cls = instance.getClass();
        while (cls.getSuperclass() != Object.class) {
            if (!cls.getSimpleName().contains("$")) {
                break;
            }
            cls = cls.getSuperclass();
        }
        return cls;
    }
}
