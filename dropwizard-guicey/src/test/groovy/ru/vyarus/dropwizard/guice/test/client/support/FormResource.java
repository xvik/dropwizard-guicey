package ru.vyarus.dropwizard.guice.test.client.support;

import com.google.common.base.Joiner;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @author Vyacheslav Rusakov
 * @since 10.10.2025
 */
@Path("/form")
public class FormResource {

    @Path("/post")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public String post(@NotNull @FormParam("name") String value,
                     @NotNull @FormParam("date") String date) {
        return "name=" + value + ", date=" + date;
    }

    @Path("/post2")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public String post2(@NotEmpty MultivaluedMap<String, String> params) {
        return Joiner.on(", ").withKeyValueSeparator('=').join(params);
    }

    @Path("/postMulti")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public String postMulti(@NotEmpty @FormParam("name") List<String> value,
                          @NotNull @FormParam("date") String date) {
        return "name=" + value + ", date=" + date;
    }

    @Path("/post2Multi")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public String post2Multi(@NotEmpty MultivaluedMap<String, String> params) {
        return Joiner.on(", ").withKeyValueSeparator('=').join(params);
    }

    @Path("/get")
    @GET
    public String get(@NotNull @QueryParam("name") String value,
                    @NotNull @QueryParam("date") String date) {
        return "name=" + value + ", date=" + date;
    }

    @Path("/getMulti")
    @GET
    public String getMulti(@NotEmpty @QueryParam("name") List<String> value,
                         @NotNull @QueryParam("date") String date) {
        return "name=" + value + ", date=" + date;
    }

    @Path("/multipart")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String multipart(
            @NotNull @FormDataParam("file") InputStream uploadedInputStream,
            @NotNull @FormDataParam("file") FormDataContentDisposition fileDetail) {
        return fileDetail.getFileName(true);
    }

    @Path("/multipart2")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String multipart2(
            @NotNull @FormDataParam("file") FormDataBodyPart file) {
        return file.getContentDisposition().getFileName();
    }

    @Path("/multipartMulti")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String multipartMulti(
            @NotNull @FormDataParam("file") List<FormDataBodyPart> file) {
        return file.get(0).getContentDisposition().getFileName();
    }

    @Path("/multipartMulti2")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String multipartMulti2(
            @NotNull @FormDataParam("file") List<FormDataContentDisposition> file) {
        return file.get(0).getFileName(true);
    }

    @Path("/multipartGeneric")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String multipartGeneric(@NotNull FormDataMultiPart multiPart) {
        Map<String, List<FormDataBodyPart>> fieldsMap = multiPart.getFields();
        return fieldsMap.get("file").get(0).getContentDisposition().getFileName();
    }
}
