package ru.vyarus.dropwizard.guice.debug.renderer.guice.support.privt;

import com.google.inject.PrivateModule;

/**
 * @author Vyacheslav Rusakov
 * @since 21.08.2019
 */
public class Inner3Module extends PrivateModule {

    @Override
    protected void configure() {
        bind(OutServ.class);
        expose(OutServ.class);
    }

    public static class OutServ {}
}
