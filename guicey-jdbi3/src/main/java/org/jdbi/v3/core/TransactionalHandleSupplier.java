package org.jdbi.v3.core;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.extension.ExtensionContext;
import org.jdbi.v3.core.extension.HandleSupplier;

import java.util.concurrent.Callable;

/**
 * Bridge have to lie in jdbi package in order have access to internal methods. Implementation is the same
 * as in {@link ConstantHandleSupplier}, except handler and config are obtained dynamically.
 *
 * @author Vyacheslav Rusakov
 * @since 31.08.2018
 */
@Singleton
public class TransactionalHandleSupplier implements HandleSupplier {

    private final Jdbi jdbi;
    private final Provider<Handle> handleProvider;

    @Inject
    public TransactionalHandleSupplier(final Jdbi jdbi, final Provider<Handle> handleProvider) {
        this.jdbi = jdbi;
        this.handleProvider = handleProvider;
    }

    @Override
    public Handle getHandle() {
        return handleProvider.get();
    }

    @Override
    public Jdbi getJdbi() {
        return jdbi;
    }

    @Override
    public <V> V invokeInContext(final ExtensionContext extensionContext, final Callable<V> task)
            throws Exception {
        // implementation copied from ConstantHandleSupplier
        final Handle handle = getHandle();
        final ExtensionContext oldExtensionContext = new ExtensionContext(
                handle.getConfig(), handle.getExtensionMethod());
        try {
            handle.acceptExtensionContext(extensionContext);
            return task.call();
        } finally {
            handle.acceptExtensionContext(oldExtensionContext);
        }
    }

    @Override
    public ConfigRegistry getConfig() {
        return jdbi.getConfig();
    }
}
