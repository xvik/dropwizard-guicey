package ru.vyarus.dropwizard.guice.cases.hkscope.support

import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerRequestFilter
import javax.ws.rs.ext.Provider

/**
 * @author Vyacheslav Rusakov
 * @since 19.01.2016
 */
@Provider
class GuiceContainerRequestFilter implements ContainerRequestFilter {

    @Override
    void filter(ContainerRequestContext requestContext) throws IOException {

    }
}
