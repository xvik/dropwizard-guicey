package ru.vyarus.dropwizard.guice.config.protect;

import io.dropwizard.lifecycle.Managed;
import ru.vyarus.dropwizard.guice.module.installer.scanner.InvisibleForScanner;

/**
 * @author Vyacheslav Rusakov
 * @since 28.01.2025
 */
public class PublicExt1 implements Managed {

    @Override
    public void start() throws Exception {
        Managed.super.start();
    }

    @Override
    public void stop() throws Exception {
        Managed.super.stop();
    }

    protected static class ProtectedExt2 implements Managed {
        @Override
        public void stop() throws Exception {
            Managed.super.stop();
        }

        @Override
        public void start() throws Exception {
            Managed.super.start();
        }
    }

    @InvisibleForScanner
    protected static class ProtectedExt4 implements Managed {
        @Override
        public void stop() throws Exception {
            Managed.super.stop();
        }

        @Override
        public void start() throws Exception {
            Managed.super.start();
        }
    }
}
