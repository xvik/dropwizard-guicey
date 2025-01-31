package ru.vyarus.dropwizard.guice.debug.renderer.guice.support.privt;

import com.google.inject.Exposed;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;

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

        bind(OService.class).to(IndirectOuterService.class);

        expose(OuterService.class);
        expose(OService.class);
    }

    @Provides @Exposed
    public OuterProviderService getService() {
        return new OuterProviderService();
    }

    public static class InnerService {}
    public static class OuterService {}

    public static class OuterProviderService {}

    public interface OService {}
    public static class IndirectOuterService implements OService {}
}
