package ru.vyarus.guicey.admin.support

import javax.inject.Inject
import javax.inject.Provider
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.GET
import javax.ws.rs.Path

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
