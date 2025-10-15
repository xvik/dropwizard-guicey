package ru.vyarus.dropwizard.guice.url.resource.support;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.MatrixParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.InputStream;

/**
 * @author Vyacheslav Rusakov
 * @since 29.09.2025
 */
@Path("/direct/")
public class DirectResource {

    @GET
    @Path("/{sm}/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("sm") String sm,
                        @QueryParam("q") String q,
                        @HeaderParam("HH") String hh,
                        @CookieParam("cc") String cc,
                        @MatrixParam("mm") String mm) {
        return Response.ok().build();
    }

    @GET
    public String nopath() {
        return "nopath";
    }

    @GET
    @Path("/{sm}/2")
    public Response get(@BeanParam MappedBean bean) {
        return Response.ok().build();
    }

    @POST
    @Path("/form")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void form(@FormParam("p1") String p1,
                     @FormParam("p2") Integer p2) {
    }

    @POST
    @Path("/multipart")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public void multipart(@FormDataParam("p1") String p1,
                          @FormDataParam("file1") InputStream file1,
                          @FormDataParam("p2") FormDataBodyPart file2) {
    }

    @POST
    @Path("/multipart")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public void multipart2(@FormDataParam("p1") String p1,
                           @FormDataParam("file") InputStream file,
                           @FormDataParam("file") FormDataContentDisposition fileDisposition) {
    }

    @Path("/entity")
    @POST
    public void post2(ModelType model) {
    }

    @Path("/entity2")
    @POST
    public void post3(@NotNull ModelType model) {
    }

    public static class ModelType {
        private String name;

        public ModelType() {
        }

        public ModelType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
