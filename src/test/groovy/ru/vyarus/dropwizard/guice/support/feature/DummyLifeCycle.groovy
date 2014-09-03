package ru.vyarus.dropwizard.guice.support.feature

import org.eclipse.jetty.util.component.LifeCycle

/**
 * @author Vyacheslav Rusakov 
 * @since 03.09.2014
 */
class DummyLifeCycle implements LifeCycle {
    @Override
    void start() throws Exception {

    }

    @Override
    void stop() throws Exception {

    }

    @Override
    boolean isRunning() {
        return false
    }

    @Override
    boolean isStarted() {
        return false
    }

    @Override
    boolean isStarting() {
        return false
    }

    @Override
    boolean isStopping() {
        return false
    }

    @Override
    boolean isStopped() {
        return false
    }

    @Override
    boolean isFailed() {
        return false
    }

    @Override
    void addLifeCycleListener(LifeCycle.Listener listener) {

    }

    @Override
    void removeLifeCycleListener(LifeCycle.Listener listener) {

    }
}
