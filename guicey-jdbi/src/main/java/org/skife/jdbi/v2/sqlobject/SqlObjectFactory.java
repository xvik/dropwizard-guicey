package org.skife.jdbi.v2.sqlobject;

/**
 * Constructs JDBI sql object proxy. Have to use JDBI package to access package-private apis.
 *
 * @author Vyacheslav Rusakov
 * @since 4.12.2016
 */
public final class SqlObjectFactory {

    private SqlObjectFactory() {
    }

    /**
     * @param cls  class to create proxy for
     * @param ding custom ding to correctly participate in transaction
     * @param <T>  type of provided class
     * @return JDBI sql object proxy
     */
    public static <T> T instance(final Class<T> cls, final HandleDing ding) {
        return SqlObject.buildSqlObject(cls, ding);
    }
}
