package ru.vyarus.dropwizard.guice.examples.rest;

import io.dropwizard.auth.Auth;
import ru.vyarus.dropwizard.guice.examples.auth.User;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

/**
 * @author Vyacheslav Rusakov
 * @since 25.01.2019
 */
@Path("/")
@Produces("text/plain")
public class AuthAwareResource {

    // authorization required
    @GET
    @Path("/auth")
    public String auth(@Auth User user) {
        return user.getName();
    }

    // authorized user must have ADMIN role
    @GET
    @Path("/adm")
    @RolesAllowed("ADMIN")
    public String admin(@Auth User user) {
        return user.getName();
    }

    // authorized user must have MRX role
    @GET
    @Path("/deny")
    @RolesAllowed("MRX")
    public String deny(@Auth User user) {
        return user.getName();
    }
}
