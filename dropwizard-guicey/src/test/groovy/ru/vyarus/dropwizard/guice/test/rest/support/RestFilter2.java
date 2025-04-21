package ru.vyarus.dropwizard.guice.test.rest.support;

import javax.inject.Singleton;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import java.io.IOException;

/**
 * @author Vyacheslav Rusakov
 * @since 22.02.2025
 */
@Provider
@Singleton
public class RestFilter2 implements ContainerResponseFilter {

    public boolean called = false;

    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) throws IOException {
        called = true;
    }
}
