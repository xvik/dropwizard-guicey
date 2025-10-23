package ru.vyarus.dropwizard.guice.url.resource.support;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
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
@Path("/iface/")
public interface ResourceDeclaration {

    @GET
    @Path("/{sm}/")
    @Produces(MediaType.APPLICATION_JSON)
    Response get(@PathParam("sm") String sm,
                        @QueryParam("q") String q,
                        @HeaderParam("HH") String hh,
                        @CookieParam("cc") String cc);

    @GET
    String nopath();

    @Path("/{sm}/2")
    Response get(MappedBean bean);

    @Path("/form")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    void form(@FormParam("p1") String p1,
                     @FormParam("p2") Integer p2);

    @Path("/multipart")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    void multipart(@FormDataParam("p1") String p1,
                          @FormDataParam("file1") InputStream file1,
                          @FormDataParam("file2") FormDataBodyPart file2);

    @Path("/multipart")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    void multipart2(@FormDataParam("p1") String p1,
                           @FormDataParam("file") InputStream file,
                           @FormDataParam("file") FormDataContentDisposition fileDisposition);
}
