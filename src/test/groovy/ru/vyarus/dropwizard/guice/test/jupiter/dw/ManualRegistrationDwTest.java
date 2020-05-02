package ru.vyarus.dropwizard.guice.test.jupiter.dw;

import com.google.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.support.TestConfiguration;
import ru.vyarus.dropwizard.guice.support.feature.DummyExceptionMapper;
import ru.vyarus.dropwizard.guice.support.feature.DummyManaged;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.TestDropwizardAppExtension;
import ru.vyarus.dropwizard.guice.test.jupiter.param.AppAdminPort;
import ru.vyarus.dropwizard.guice.test.jupiter.param.AppPort;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

/**
 * @author Vyacheslav Rusakov
 * @since 03.05.2020
 */
public class ManualRegistrationDwTest {

    @RegisterExtension
    static TestDropwizardAppExtension app = TestDropwizardAppExtension.forApp(AutoScanApplication.class)
            .config("src/test/resources/ru/vyarus/dropwizard/guice/config.yml")
            .configOverrides("foo=2", "bar=12")
            .randomPorts()
            .restMapping("api")
            .hooks(Hook.class)
            .hooks(builder -> builder.disableExtensions(DummyManaged.class))
            .create();

    @Inject
    TestConfiguration config;

    @Test
    void checkCorrectWiring(GuiceyConfigurationInfo info, @AppPort int port, @AppAdminPort int adminPort) {
        Assertions.assertEquals(config.foo, 2);
        Assertions.assertEquals(config.bar, 12);
        Assertions.assertEquals(config.baa, 4);

        Assertions.assertNotEquals(8080, port);
        Assertions.assertNotEquals(8081, adminPort);

        Assertions.assertNotNull(info);
        Assertions.assertTrue(info.getExtensionsDisabled().contains(DummyManaged.class));
        Assertions.assertTrue(info.getExtensionsDisabled().contains(DummyExceptionMapper.class));

        Response response = ClientBuilder.newClient()
                .target("http://localhost:" + port + "/api/dummy/")
                .request()
                .buildGet()
                .invoke();

        Assertions.assertEquals(200, response.getStatus());
    }

    public static class Hook implements GuiceyConfigurationHook {

        @Override
        public void configure(GuiceBundle.Builder builder) {
            builder.disableExtensions(DummyExceptionMapper.class);
        }
    }
}
