package ru.vyarus.dropwizard.guice.debug.renderer.guice.support;

import com.google.inject.AbstractModule;

import jakarta.ws.rs.Path;

/**
 * @author Vyacheslav Rusakov
 * @since 19.09.2019
 */
public class TransitiveModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Res1.class);
        bind(Res2.class);
        install(new Inner());
    }

    public static class Inner extends AbstractModule {
        @Override
        protected void configure() {
            bind(Res3.class);
            install(new SubInner());
        }
    }

    public static class SubInner extends AbstractModule {
        @Override
        protected void configure() {
            bind(Res4.class);
        }
    }

    @Path("/1")
    public static class Res1 {}

    @Path("/2")
    public static class Res2 {}

    @Path("/3")
    public static class Res3 {}

    @Path("/4")
    public static class Res4 {}
}
