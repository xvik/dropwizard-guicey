package ru.vyarus.dropwizard.guice.debug.renderer.guice.support;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;

import jakarta.ws.rs.Path;

/**
 * @author Vyacheslav Rusakov
 * @since 03.09.2019
 */
public class DisableExtensionModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Res1.class);
        bind(Res2.class).to(Res2Impl.class);
        bind(Res3.class).toInstance(new Res3Impl());
        bind(Res4.class).toProvider(Res4Provider.class);
        bind(Res5.class).toProvider(new Res5Provider());
    }

    @Path("/1")
    public static class Res1 {}

    @Path("/2")
    public static class Res2 {}

    public static class Res2Impl extends Res2 {}

    @Path("/3")
    public static class Res3 {}

    public static class Res3Impl extends Res3 {}

    @Path("/4")
    public static class Res4 {}

    public static class Res4Provider implements Provider<Res4> {
        @Override
        public Res4 get() {
            return new Res4();
        }
    }

    @Path("/4")
    public static class Res5 {}

    public static class Res5Provider implements Provider<Res5> {
        @Override
        public Res5 get() {
            return new Res5();
        }
    }
}
