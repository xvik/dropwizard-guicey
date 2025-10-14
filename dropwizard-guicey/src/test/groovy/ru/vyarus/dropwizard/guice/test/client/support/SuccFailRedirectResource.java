package ru.vyarus.dropwizard.guice.test.client.support;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import ru.vyarus.dropwizard.guice.url.AppUrlBuilder;

/**
 * @author Vyacheslav Rusakov
 * @since 08.10.2025
 */
@Path("/status")
public class SuccFailRedirectResource {

    @Inject
    AppUrlBuilder urlBuilder;

    @Path("/get")
    @GET
    public String get() {
        return "ok";
    }

    @Path("/post")
    @POST
    public String post(String entity) {
        return entity;
    }

    @Path("/error")
    @GET
    public String error() {
        throw new IllegalStateException("err");
    }

    @Path("/redirect")
    @GET
    public Response redirect() {
        return Response.seeOther(
                urlBuilder.rest(SuccFailRedirectResource.class).method(SuccFailRedirectResource::get).buildUri()
        ).build();
    }

}
