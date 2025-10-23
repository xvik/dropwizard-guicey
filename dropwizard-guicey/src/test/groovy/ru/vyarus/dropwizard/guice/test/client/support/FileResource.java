package ru.vyarus.dropwizard.guice.test.client.support;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
