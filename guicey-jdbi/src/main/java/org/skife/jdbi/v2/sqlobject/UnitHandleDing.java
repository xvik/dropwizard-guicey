package org.skife.jdbi.v2.sqlobject;

import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.SqlObjectContext;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Unit of work aware handle ding. Required to re-use thread bound handle in JDBI sql proxies.
 * Have to use JDBI package to access package-private apis.
 *
 * @author Vyacheslav Rusakov
 * @since 4.12.2016
 */
public class UnitHandleDing implements HandleDing {

    @Inject
    private Provider<Handle> handleProvider;

    @Override
    public Handle getHandle() {
        return handleProvider.get();
    }

    @Override
    public void release(final String name) {
        // no need to track it here because unit of work control scope
    }

    @Override
    public void retain(final String name) {
        // no need to track it here because unit of work control scope
    }

    @SuppressWarnings("PMD.LinguisticNaming")
    @Override
    public SqlObjectContext setContext(final SqlObjectContext context) {
        final Handle handle = getHandle();
        final SqlObjectContext oldContext = handle.getSqlObjectContext();
        handle.setSqlObjectContext(context);
        return oldContext == null ? new SqlObjectContext() : oldContext;
    }
}
