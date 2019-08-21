package ru.vyarus.dropwizard.guice.debug.renderer.guice.support.privt;

import com.google.inject.AbstractModule;

/**
 * @author Vyacheslav Rusakov
 * @since 21.08.2019
 */
public class Inner2Module extends AbstractModule {

    @Override
    protected void configure() {
        install(new Inner3Module());
        bind(InnerService2.class);
    }

    public static class InnerService2 {}
}
