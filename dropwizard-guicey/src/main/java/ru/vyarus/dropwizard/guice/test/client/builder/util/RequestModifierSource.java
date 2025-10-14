package ru.vyarus.dropwizard.guice.test.client.builder.util;

import com.google.common.collect.ImmutableList;
import ru.vyarus.dropwizard.guice.module.installer.util.StackUtils;
import ru.vyarus.dropwizard.guice.test.client.ResourceClient;
import ru.vyarus.dropwizard.guice.test.client.TestClient;
import ru.vyarus.dropwizard.guice.test.client.TestRestClient;
import ru.vyarus.dropwizard.guice.test.client.builder.TestClientDefaults;
import ru.vyarus.dropwizard.guice.test.client.builder.TestClientRequestBuilder;
import ru.vyarus.dropwizard.guice.test.client.builder.TestRequestConfig;

import java.util.List;

/**
 * Utility to detect configuration source.
 *
 * @author Vyacheslav Rusakov
 * @since 03.10.2025
 */
public final class RequestModifierSource {

    private static final List<Class<?>> INFRA = ImmutableList.of(
            TestRequestConfig.class,
            TestClientRequestBuilder.class,
            TestClientDefaults.class,
            TestClient.class,
            TestRestClient.class,
            ResourceClient.class,
            RequestModifierSource.class
    );

    private RequestModifierSource() {
    }

    /**
     * @return test client calling source
     */
    public static String getSource() {
        return StackUtils.getCallerSource(INFRA);
    }
}
