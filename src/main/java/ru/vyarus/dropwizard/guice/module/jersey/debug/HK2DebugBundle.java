package ru.vyarus.dropwizard.guice.module.jersey.debug;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.JerseyFeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.JerseyManaged;
import ru.vyarus.dropwizard.guice.module.jersey.debug.service.ContextDebugService;
import ru.vyarus.dropwizard.guice.module.jersey.debug.service.GuiceInstanceListener;
import ru.vyarus.dropwizard.guice.module.jersey.debug.service.HK2DebugFeature;

/**
 * Bundle enables debug services to check correct extensions instantiation:
 * <ul>
 * <li>{@link JerseyManaged} annotated service must be
 * instantiated by HK2 and not guice</li>
 * <li>Other services must be instantiated only in guice</li>
 * </ul>
 * <p>
 * When enabled, exception will be thrown when service instantiated outside of assumed bound (or duplicate instantiation
 * occur).
 * <p>
 * Checked only beans registered by {@link ru.vyarus.dropwizard.guice.module.installer.install.JerseyInstaller}
 * ({@link ru.vyarus.dropwizard.guice.module.installer.feature.jersey.ResourceInstaller},
 * {@link ru.vyarus.dropwizard.guice.module.installer.feature.jersey.provider.JerseyProviderInstaller} or any 3rd party
 * installer implementing jersey installer interface).
 * <p>
 * Module intended to be used in tests.
 * {@link ru.vyarus.dropwizard.guice.module.jersey.debug.service.ContextDebugService} collects all tracked classes
 * instantiated by both guice and HK2 and may provide lists of classes accordingly. It may be used in test conditions.
 *
 * @author Vyacheslav Rusakov
 * @since 15.01.2016
 */
public class HK2DebugBundle implements GuiceyBundle {

    @Override
    public void initialize(final GuiceyBootstrap bootstrap) {
        bootstrap
                // register to guarantee installer presence (e.g. in manual mode)
                .installers(JerseyFeatureInstaller.class)
                .extensions(HK2DebugFeature.class)
                .modules(new HK2DebugModule());
    }

    /**
     * Guice module with scope validation services.
     */
    public static class HK2DebugModule extends AbstractModule {
        @Override
        protected void configure() {
            final GuiceInstanceListener listener = new GuiceInstanceListener();
            requestInjection(listener);
            bindListener(Matchers.any(), listener);

            bind(ContextDebugService.class);
//            bind(HK2InstanceListener.class);
        }
    }
}
