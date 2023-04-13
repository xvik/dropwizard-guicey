package ru.vyarus.dropwizard.guice.support.request

import com.google.common.base.Preconditions
import com.google.inject.Inject
import com.google.inject.Provider

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.Response

/**
 * Rest service for validation of guice request scoped beans support.
 *
 * @author Vyacheslav Rusakov 
 * @since 04.10.2014
 */
@Path("/dummy")
@Produces('application/json')
class RequestBeansResource {

    @Inject
    Provider<RequestScopedBean> requestScopedBeanProvider
    @Inject
    Provider<HttpServletRequest> requestProvider
    @Inject
    Provider<HttpServletResponse> responseProvider

    @GET
    @Path("/")
    public Response check() {
        Preconditions.checkNotNull(requestProvider.get())
        Preconditions.checkNotNull(responseProvider.get())
        Preconditions.checkState(requestScopedBeanProvider.get().foo().equals('foo'));
        return Response.ok().build();
    }
}
