package ru.vyarus.dropwizard.guice.test.general;

import org.glassfish.jersey.apache5.connector.Apache5ConnectorProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.test.TestSupport;

/**
 * @author Vyacheslav Rusakov
 * @since 12.09.2025
 */
public class ApacheFactoryTest {

    @Test
    void testApacheFactoryShortcut() throws Exception{
        Class<?> cls = TestSupport.build(AutoScanApplication.class)
                .useApacheClient()
                .runCore(injector -> TestSupport
                        .getContextClient().getClient().getConfiguration().getConnectorProvider().getClass());
        Assertions.assertEquals(Apache5ConnectorProvider.class, cls);
    }
}
