package ru.vyarus.dropwizard.guice.config.debug.renderer.support;

import com.google.inject.AbstractModule;
import ru.vyarus.dropwizard.guice.config.debug.renderer.GuiceRendererCasesTest;

/**
 * @author Vyacheslav Rusakov
 * @since 20.08.2019
 */
public class OverrideModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(GuiceRendererCasesTest.BindService.class).to(GuiceRendererCasesTest.OverrideService.class);
        bind(GuiceRendererCasesTest.BindService2.class).toInstance(new GuiceRendererCasesTest.BindService2() {});
    }
}
