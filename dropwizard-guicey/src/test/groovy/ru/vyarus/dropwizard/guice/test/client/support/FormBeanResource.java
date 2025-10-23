package ru.vyarus.dropwizard.guice.test.client.support;

import javax.validation.constraints.NotNull;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.InputStream;
import java.util.List;

/**
 * @author Vyacheslav Rusakov
 * @since 10.10.2025
 */
@Path("/formbean")
public class FormBeanResource {

    @Path("/post")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public String post(@NotNull @BeanParam SimpleBean bean) {
        return "name=" + bean.value + ", date=" + bean.date;
    }

    public static class SimpleBean {
        @FormParam("name")
        public String value;

        @FormParam("date")
        public String date;

        public SimpleBean() {
        }

        public SimpleBean(String value, String date) {
            this.value = value;
            this.date = date;
        }
    }

    @Path("/postMulti")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public String postMulti(@NotNull @BeanParam SimpleMultiBean bean) {
        return "name=" + bean.value + ", date=" + bean.date;
    }

    public static class SimpleMultiBean {
        @FormParam("name")
        public List<String> value;

        @FormParam("date")
        public String date;

        public SimpleMultiBean() {
        }

        public SimpleMultiBean(List<String> value, String date) {
            this.value = value;
            this.date = date;
        }
    }

    @Path("/multipart")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String multipart(@NotNull @BeanParam MultipartBean bean) {
        return bean.detail.getFileName(true);
    }

    public static class MultipartBean {
        @FormDataParam("file")
        public InputStream stream;

        @FormDataParam("file")
        public FormDataContentDisposition detail;

        public MultipartBean() {
        }

        public MultipartBean(InputStream stream, FormDataContentDisposition detail) {
            this.stream = stream;
            this.detail = detail;
        }
    }

    @Path("/multipart2")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String multipart2(@NotNull @BeanParam MultipartBean2 bean) {
        return bean.file.getContentDisposition().getFileName();
    }

    public static class MultipartBean2 {
        @FormDataParam("file")
        public FormDataBodyPart file;

        public MultipartBean2() {
        }

        public MultipartBean2(FormDataBodyPart file) {
            this.file = file;
        }
    }

    @Path("/multipartMulti")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String multipartMulti(@NotNull @BeanParam MultipartMultiBean bean) {
        return bean.file.get(0).getContentDisposition().getFileName();
    }

    public static class MultipartMultiBean {
        @FormDataParam("file")
        public List<FormDataBodyPart> file;

        public MultipartMultiBean() {
        }

        public MultipartMultiBean(List<FormDataBodyPart> file) {
            this.file = file;
        }
    }

    @Path("/multipartMulti2")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String multipartMulti2(@NotNull @BeanParam MultipartMultiBean2 data) {
        return data.file.get(0).getFileName(true);
    }

    public static class MultipartMultiBean2 {
        @FormDataParam("file")
        public List<FormDataContentDisposition> file;

        public MultipartMultiBean2() {
        }

        public MultipartMultiBean2(List<FormDataContentDisposition> file) {
            this.file = file;
        }
    }
}
