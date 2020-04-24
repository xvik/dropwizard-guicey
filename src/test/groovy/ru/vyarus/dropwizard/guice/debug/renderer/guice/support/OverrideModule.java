package ru.vyarus.dropwizard.guice.debug.renderer.guice.support;

import com.google.inject.AbstractModule;
import ru.vyarus.dropwizard.guice.debug.renderer.guice.support.exts.BindService;
import ru.vyarus.dropwizard.guice.debug.renderer.guice.support.exts.BindService2;
import ru.vyarus.dropwizard.guice.debug.renderer.guice.support.exts.OverrideService;

/**
 * @author Vyacheslav Rusakov
 * @since 20.08.2019
 */
public class OverrideModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(BindService.class).to(OverrideService.class);
        bind(BindService2.class).toInstance(new BindService2() {});
    }
}
