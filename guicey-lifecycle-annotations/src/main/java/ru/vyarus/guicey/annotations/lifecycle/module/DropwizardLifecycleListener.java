package ru.vyarus.guicey.annotations.lifecycle.module;

import io.dropwizard.lifecycle.Managed;
import io.dropwizard.lifecycle.ServerLifecycleListener;
import org.eclipse.jetty.server.Server;
import ru.vyarus.guicey.annotations.lifecycle.PostStartup;
import ru.vyarus.guicey.annotations.lifecycle.module.collector.MethodsCollector;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Listener bean used to process annotated methods in appropriate dropwizard lifecycle phases.
 *
 * @author Vyacheslav Rusakov
 * @since 26.11.2018
 */
public class DropwizardLifecycleListener implements ServerLifecycleListener, Managed {

    private final MethodsCollector collector;

    public DropwizardLifecycleListener(final MethodsCollector collector) {
        this.collector = collector;
    }

    @Override
    public void start() throws Exception {
        collector.call(PostConstruct.class);
    }

    @Override
    public void serverStarted(final Server server) {
        collector.call(PostStartup.class);
    }

    @Override
    public void stop() throws Exception {
        collector.safeCall(PreDestroy.class);
    }
}
