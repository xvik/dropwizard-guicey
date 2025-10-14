package ru.vyarus.dropwizard.guice.test.client.support;

import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;

/**
 * @author Vyacheslav Rusakov
 * @since 07.10.2025
 */
public class ClientApp extends DefaultTestApp {

    @Override
    protected GuiceBundle configure() {
        return GuiceBundle.builder()
                .extensions(
                        Resource.class,
                        SuccFailRedirectResource.class,
                        FileResource.class,
                        FormResource.class,
                        FormBeanResource.class,
                        ErrorsResource.class,
                        MatrixResource.class,
                        PrimitivesResource.class)
                .build();
    }
}
