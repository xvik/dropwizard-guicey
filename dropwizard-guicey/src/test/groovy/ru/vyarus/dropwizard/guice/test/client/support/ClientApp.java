package ru.vyarus.dropwizard.guice.test.client.support;

import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Environment;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
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

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
        environment.jersey().register(MultiPartFeature.class);
    }
}
