package ru.vyarus.dropwizard.guice.config.debug.util.support;

import com.google.inject.Binder;
import com.google.inject.Module;

/**
 * @author Vyacheslav Rusakov
 * @since 14.09.2019
 */
public class WithAnonymous {
    public static Module ANONYMOUS = new Module() {
        @Override
        public void configure(Binder binder) {
        }
    };

}
