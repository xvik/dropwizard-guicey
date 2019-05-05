package ru.vyarus.dropwizard.guice.injector.jersey.scope;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;

/**
 * @author Vyacheslav Rusakov
 * @since 23.04.2019
 */
public class ThreadScope implements Scope {

    public static final ThreadLocal<?> threadlocal = new  ThreadLocal<>();

    @Override
    public <T> Provider<T> scope(Key<T> key, Provider<T> unscoped) {
        return new Provider<T> () {
            public T get() {
                if (null != threadlocal.get()) {
                    return (T) threadlocal.get();
                } else {
                    throw new IllegalStateException("No instance found in current thread");
                }
            }
        };
    }
}
