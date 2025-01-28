package ru.vyarus.dropwizard.guice.config.protect;

import io.dropwizard.lifecycle.Managed;
import ru.vyarus.dropwizard.guice.module.installer.scanner.InvisibleForScanner;

/**
 * @author Vyacheslav Rusakov
 * @since 28.01.2025
 */
@InvisibleForScanner
public class ProtectedExt3 implements Managed {

    @Override
    public void start() throws Exception {
        Managed.super.start();
    }

    @Override
    public void stop() throws Exception {
        Managed.super.stop();
    }
}
