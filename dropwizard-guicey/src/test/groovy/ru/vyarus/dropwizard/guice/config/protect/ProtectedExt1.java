package ru.vyarus.dropwizard.guice.config.protect;

import io.dropwizard.lifecycle.Managed;

/**
 * @author Vyacheslav Rusakov
 * @since 28.01.2025
 */
class ProtectedExt1 implements Managed {

    @Override
    public void start() throws Exception {
        Managed.super.start();
    }

    @Override
    public void stop() throws Exception {
        Managed.super.stop();
    }
}
