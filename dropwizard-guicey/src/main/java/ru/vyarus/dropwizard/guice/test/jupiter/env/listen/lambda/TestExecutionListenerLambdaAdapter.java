package ru.vyarus.dropwizard.guice.test.jupiter.env.listen.lambda;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import ru.vyarus.dropwizard.guice.test.jupiter.env.listen.EventContext;
import ru.vyarus.dropwizard.guice.test.jupiter.env.listen.TestExecutionListener;

/**
 * An adapter for {@link ru.vyarus.dropwizard.guice.test.jupiter.env.listen.TestExecutionListener} to be able to
 * register each listener method with a lambada (more suitable for builder style, rather than direct interface
 * implementation).
 *
 * @author Vyacheslav Rusakov
 * @since 20.02.2025
 */
public class TestExecutionListenerLambdaAdapter implements TestExecutionListener {

    private final Multimap<ListenerEvent, LambdaTestListener> listeners = ArrayListMultimap.create();

    /**
     * Add lambda as an event listener.
     *
     * @param event target event
     * @param listener listener to add
     */
    public void listen(final ListenerEvent event, final LambdaTestListener listener) {
        listeners.put(event, listener);
    }

    @Override
    public void started(final EventContext context) {
        callListeners(context, ListenerEvent.Started);
    }

    @Override
    public void beforeAll(final EventContext context) {
        callListeners(context, ListenerEvent.BeforeAll);
    }

    @Override
    public void beforeEach(final EventContext context) {
        callListeners(context, ListenerEvent.BeforeEach);
    }

    @Override
    public void afterEach(final EventContext context) {
        callListeners(context, ListenerEvent.AfterEach);
    }

    @Override
    public void afterAll(final EventContext context) {
        callListeners(context, ListenerEvent.AfterAll);
    }

    @Override
    public void stopped(final EventContext context) {
        callListeners(context, ListenerEvent.Stopped);
    }

    private void callListeners(final EventContext context, final ListenerEvent event) {
        for (LambdaTestListener listener : listeners.get(event)) {
            listener.onTestEvent(context);
        }
    }

}
