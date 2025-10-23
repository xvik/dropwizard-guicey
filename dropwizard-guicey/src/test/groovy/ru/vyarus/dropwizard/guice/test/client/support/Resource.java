package ru.vyarus.dropwizard.guice.test.client.support;

import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.RuntimeDelegate;
import org.eclipse.jetty.http.HttpHeader;
import ru.vyarus.dropwizard.guice.test.client.support.sub.SubResource;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * @author Vyacheslav Rusakov
 * @since 07.10.2025
 */
@Path("/root")
public class Resource {

    @GET
    @Path("/get")
    public List<Integer> get() {
        return Arrays.asList(1, 2, 3);
    }

    @GET
    @Path("/get/{name}")
    public List<Integer> get(@PathParam("name") String param) {
        return Arrays.asList(4, 5, 6);
    }

    @GET
    @Path("/filled")
    @Produces(MediaType.TEXT_PLAIN)
    public Response filled() {
        return Response.ok("OK")
                .language(Locale.CANADA)
                .header("HH", "3")
                .header(HttpHeader.X_POWERED_BY.toString(), "4")
                .cookie(new NewCookie("C", "12"))
                .cacheControl(RuntimeDelegate.getInstance().createHeaderDelegate(CacheControl.class)
                        .fromString("max-age=604800, must-revalidate"))
                .build();
    }

    @DELETE
    @Path("/del")
    public void del() {
    }

    @DELETE
    @Path("/delete")
    public int delete() {
        return 1;
    }

    @POST
    @Path("/post")
    public String post(String text) {
        return text;
    }

    @PUT
    @Path("/put")
    public String put(String text) {
        return text;
    }

    @PATCH
    @Path("/patch")
    public String patch(String text) {
        return text;
    }

    @Path("/sub")
    public SubResource sub() {
        return new SubResource();
    }

    @Path("/entity")
    @POST
    public String post2(ModelType model) {
        return model.getName();
    }

    @Path("/entity2")
    @POST
    public String post3(@NotNull ModelType model) {
        return model.getName();
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
