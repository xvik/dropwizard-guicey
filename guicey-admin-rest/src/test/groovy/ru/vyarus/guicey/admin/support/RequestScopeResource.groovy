package ru.vyarus.guicey.admin.support

import jakarta.inject.Inject
import jakarta.inject.Provider
import jakarta.servlet.http.HttpServletRequest
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path

/**
 * @author Vyacheslav Rusakov 
 * @since 03.09.2015
 */
@Path("/request")
class RequestScopeResource {

    @Inject
    Provider<HttpServletRequest> requestProvider;

    @GET
    @Path("/")
    public String hello() {
        // check request scoped beans work from admin context
        requestProvider.get();
        return "hello"
    }
}
