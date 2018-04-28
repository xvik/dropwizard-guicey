package ru.vyarus.dropwizard.guice.cases.hkscope.support

import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.GuiceManaged

import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerRequestFilter
import javax.ws.rs.ext.Provider

/**
 * @author Vyacheslav Rusakov
 * @since 19.01.2016
 */
@Provider
@GuiceManaged
class GuiceContainerRequestFilter implements ContainerRequestFilter {

    @Override
    void filter(ContainerRequestContext requestContext) throws IOException {

    }
}
