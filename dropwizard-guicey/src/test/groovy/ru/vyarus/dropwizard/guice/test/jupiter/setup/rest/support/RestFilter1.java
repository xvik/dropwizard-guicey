package ru.vyarus.dropwizard.guice.test.jupiter.setup.rest.support;

import jakarta.inject.Singleton;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

/**
 * @author Vyacheslav Rusakov
 * @since 22.02.2025
 */
@Provider
@Singleton
public class RestFilter1 implements ContainerResponseFilter {

    public boolean called = false;

    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) throws IOException {
        called = true;
    }
}
