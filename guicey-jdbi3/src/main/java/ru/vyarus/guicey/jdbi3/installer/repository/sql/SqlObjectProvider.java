package ru.vyarus.guicey.jdbi3.installer.repository.sql;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.TransactionalHandleSupplier;
import org.jdbi.v3.core.extension.Extensions;
import org.jdbi.v3.core.extension.NoSuchExtensionException;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

/**
 * Factory re-implements {@code Jdbi.onDemand(Class)} in order to create proxy, using transactional handler
 * (jdbi on-demand objects supposed to open-close connection on each call).
 * <p>
 * Proxy is not created immediately because during proxy creation config is created for each method and so
 * if some global row mapper will be registered after this moment, method config will not know about it.
 * Provider is created just before injector creation and all mappers are registered just after injector creation,
 * so without laziness nothing would work as planned.
 *
 * @param <T> sql proxy type
 * @author Vyacheslav Rusakov
 * @since 13.09.2018
 */
public class SqlObjectProvider<T> implements Provider<T> {

    @Inject
    private Jdbi jdbi;
    @Inject
    private TransactionalHandleSupplier handleProvider;

    private final Class<T> extensionType;

    @SuppressWarnings("PMD.AvoidUsingVolatile")
    private volatile T res;

    public SqlObjectProvider(final Class<T> extensionType) {
        this.extensionType = extensionType;
    }

    @Override
    public T get() {
        // lazy sql proxy creation
        if (res == null) {
            synchronized (this) {
                if (res == null) {
                    res = create();
                }
            }
        }
        return res;
    }

    /**
     * Method used only for testing.
     *
     * @return true if jdbi proxy created, false if not
     */
    public boolean isInitialized() {
        return res != null;
    }

    private T create() {
        // Jdbi::onDemand(Class)
        if (!extensionType.isInterface()) {
            throw new IllegalArgumentException("On-demand extensions are only supported for interfaces.");
        }

        // OnDemandExtensions::create(Jdbi, Class)
        // We don't need to create jdk proxy here -  extension instance is enough.
        return jdbi.getConfig(Extensions.class)
                .findFor(extensionType, handleProvider)
                .orElseThrow(() -> new NoSuchExtensionException("Extension not found: " + extensionType));
    }
}
