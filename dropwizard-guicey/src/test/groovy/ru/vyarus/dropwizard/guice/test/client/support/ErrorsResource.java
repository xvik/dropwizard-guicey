package ru.vyarus.dropwizard.guice.test.client.support;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

/**
 * @author Vyacheslav Rusakov
 * @since 12.10.2025
 */
@Path("/errors")
public class ErrorsResource {

    @Path("/bad")
    @GET
    public Response bad() {
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @Path("/unauth")
    @GET
    public Response unauth() {
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    @Path("/forbid")
    @GET
    public Response forbid() {
        return Response.status(Response.Status.FORBIDDEN).build();
    }

    @Path("/notacc")
    @GET
    public Response notacc() {
        return Response.status(Response.Status.NOT_ACCEPTABLE).build();
    }

    @Path("/unsupported")
    @GET
    public Response unsupported() {
        return Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).build();
    }

    @Path("/error")
    @GET
    public Response error() {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    @Path("/unavailable")
    @GET
    public Response unavailable() {
        return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
    }

    @Path("/customClient")
    @GET
    public Response customClient() {
        return Response.status(Response.Status.PRECONDITION_FAILED).build();
    }

    @Path("/customServer")
    @GET
    public Response customServer() {
        return Response.status(Response.Status.BAD_GATEWAY).build();
    }

    @Path("/customRedirect")
    @GET
    public Response customRedirect() {
        return Response.status(Response.Status.USE_PROXY).build();
    }


    @Path("/informal")
    @GET
    public Response informal() {
        return Response.status(700).build();
    }
}
