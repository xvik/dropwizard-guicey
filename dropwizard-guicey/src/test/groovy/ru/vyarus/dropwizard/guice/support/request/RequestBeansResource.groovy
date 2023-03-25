package ru.vyarus.dropwizard.guice.support.request

import com.google.common.base.Preconditions
import com.google.inject.Inject
import com.google.inject.Provider

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.Response

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
