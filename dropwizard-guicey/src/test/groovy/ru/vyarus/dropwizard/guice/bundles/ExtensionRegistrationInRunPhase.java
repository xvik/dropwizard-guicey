package ru.vyarus.dropwizard.guice.bundles;

import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.lifecycle.Managed;
import javax.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo;
import ru.vyarus.dropwizard.guice.module.context.info.ExtensionItemInfo;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyEnvironment;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;

/**
 * @author Vyacheslav Rusakov
 * @since 13.03.2025
 */
@TestGuiceyApp(ExtensionRegistrationInRunPhase.App.class)
public class ExtensionRegistrationInRunPhase {

    @Inject
    GuiceyConfigurationInfo info;

    @Test
    void testExtensionRegistrationInRunPhase() {
        final ExtensionItemInfo ext = info.getInfo(ManagedBean.class);
        Assertions.assertNotNull(ext);
    }

    public static class App extends DefaultTestApp {
        @Override
        public void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(new GuiceyBundle() {
                        @Override
                        public void run(GuiceyEnvironment environment) throws Exception {
                            environment.extensions(ManagedBean.class);
                        }
                    })
                    .printLifecyclePhasesDetailed()
                    .build());
        }
    }

    public static class ManagedBean implements Managed {}
}
