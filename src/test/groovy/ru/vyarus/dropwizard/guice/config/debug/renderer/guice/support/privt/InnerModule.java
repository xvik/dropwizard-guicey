package ru.vyarus.dropwizard.guice.config.debug.renderer.guice.support.privt;

import com.google.inject.PrivateModule;

/**
 * @author Vyacheslav Rusakov
 * @since 21.08.2019
 */
public class InnerModule extends PrivateModule {

    @Override
    protected void configure() {
        install(new Inner2Module());
        bind(InnerService.class);
        bind(OuterService.class);

        expose(OuterService.class);
    }

    public static class InnerService {}
    public static class OuterService {}
}
