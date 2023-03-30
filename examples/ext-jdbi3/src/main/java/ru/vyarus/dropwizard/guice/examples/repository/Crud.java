package ru.vyarus.dropwizard.guice.examples.repository;

import ru.vyarus.dropwizard.guice.examples.model.IdEntity;
import ru.vyarus.guicey.jdbi3.tx.InTransaction;

import java.util.ConcurrentModificationException;

/**
 * @param <T> entity type
 * @author Vyacheslav Rusakov
 * @since 01.11.2018
 */
public interface Crud<T extends IdEntity> {

    @InTransaction
    default T save(final T entry) {
        // hibernate-like optimistic locking mechanism: provided entity must have the same version as in database
        if (entry.getId() == 0) {
            entry.setVersion(1);
            entry.setId(insert(entry));
        } else {
            final int ver = entry.getVersion();
            entry.setVersion(ver + 1);
            if (update(entry) == 0) {
                throw new ConcurrentModificationException(String.format(
                        "Concurrent modification for object %s %s version %s",
                        entry.getClass().getName(), entry.getId(), ver));
            }
        }
        return entry;
    }

    long insert(T entry);

    int update(T entry);
}
