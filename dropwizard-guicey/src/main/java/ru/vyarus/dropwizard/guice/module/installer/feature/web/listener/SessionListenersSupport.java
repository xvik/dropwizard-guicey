package ru.vyarus.dropwizard.guice.module.installer.feature.web.listener;

import com.google.common.base.Joiner;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import io.dropwizard.jetty.MutableServletContextHandler;
import io.dropwizard.lifecycle.Managed;
import org.eclipse.jetty.server.session.SessionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils;

import java.util.EventListener;
import java.util.stream.Collectors;

/**
 * Session listeners can't be registered immediately in installer, because it will execute before
 * application run method and so session will not be configured. Class used as session listeners holder
 * and register them later using managed lifecycle.
 *
 * @author Vyacheslav Rusakov
 * @since 08.08.2016
 */
public class SessionListenersSupport implements Managed {
    private final Logger logger = LoggerFactory.getLogger(WebListenerInstaller.class);

    private final boolean failWithoutSession;
    private final Multimap<MutableServletContextHandler, EventListener> listeners = LinkedListMultimap.create();

    public SessionListenersSupport(final boolean failWithoutSession) {
        this.failWithoutSession = failWithoutSession;
    }

    public void add(final MutableServletContextHandler environment, final EventListener listener) {
        listeners.put(environment, listener);
    }

    @Override
    public void start() throws Exception {
        for (MutableServletContextHandler environment : listeners.keySet()) {
            final SessionHandler sessionHandler = environment.getSessionHandler();
            if (sessionHandler == null) {
                final String msg = String.format(
                        "Can't register session listeners for %s because sessions support is not enabled: %s",
                        environment.getDisplayName().toLowerCase(),
                        Joiner.on(',').join(listeners.get(environment).stream()
                                .map(it -> FeatureUtils.getInstanceClass(it).getSimpleName())
                                .collect(Collectors.toList())));
                if (failWithoutSession) {
                    throw new IllegalStateException(msg);
                } else {
                    logger.warn(msg);
                }
            } else {
                listeners.get(environment).forEach(sessionHandler::addEventListener);
            }
        }
    }

    @Override
    public void stop() throws Exception {
        // not needed
    }
}
