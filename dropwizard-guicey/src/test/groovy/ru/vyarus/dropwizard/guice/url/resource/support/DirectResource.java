package ru.vyarus.dropwizard.guice.url.resource.support;

import javax.validation.constraints.NotNull;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
