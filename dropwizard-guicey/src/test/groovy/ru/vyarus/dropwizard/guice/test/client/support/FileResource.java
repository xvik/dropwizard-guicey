package ru.vyarus.dropwizard.guice.test.client.support;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * @author Vyacheslav Rusakov
 * @since 09.10.2025
 */
@Path("/file")
@Produces(MediaType.APPLICATION_OCTET_STREAM)
public class FileResource {

    @GET
    @Path("/download")
    public Response download() {
         return Response.ok(getClass().getResourceAsStream("/logback.xml"))
                 .header("Content-Disposition", "attachment; filename=logback.xml")
                 .build();
    }
}
