package ru.vyarus.dropwizard.guice.url.resource.support;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.InputStream;
import java.util.Date;
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
                     @NotNull @FormParam("date") Date date) {
        return "name=" + value + ", date=" + date;
    }

    @Path("/post2")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public String post2(@NotEmpty MultivaluedMap<String, Object> params) {
        return Joiner.on(", ").withKeyValueSeparator('=').join(params);
    }

    @Path("/postMulti")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public String postMulti(@NotEmpty @FormParam("name") List<String> value,
                          @NotNull @FormParam("date") Date date) {
        return "name=" + value + ", date=" + date;
    }

    @Path("/multipartMulti2")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String multipartMulti2(
            @NotNull @FormDataParam("file") List<FormDataContentDisposition> file,
            @NotNull @FormDataParam("file") List<InputStream> data) {
        Preconditions.checkState(file.size() == data.size());
        return file.get(0).getFileName(true);
    }

    @Path("/multipartGeneric")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String multipartGeneric(@NotNull FormDataMultiPart multiPart) {
        Map<String, List<FormDataBodyPart>> fieldsMap = multiPart.getFields();
        return fieldsMap.get("file").get(0).getFileName().get();
    }
}
