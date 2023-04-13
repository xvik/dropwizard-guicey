package ru.vyarus.guicey.gsp.support.app

import ru.vyarus.guicey.gsp.views.template.Template
import ru.vyarus.guicey.gsp.views.template.TemplateView

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.Response

/**
 * @author Vyacheslav Rusakov
 * @since 14.01.2019
 */
@Path("/app/sample/")
@Template("/app/sample.ftl")
class SampleTemplateResource {

    @Path("/{name}")
    @GET
    SampleModel get(@PathParam("name") String name) {
        return new SampleModel(name: name);
    }

    @Path("/error")
    @GET
    SampleModel error() {
        throw new IllegalStateException("error");
    }

    @Path("/error2")
    @GET
    SampleModel error2() {
        throw new WebApplicationException("error");
    }

    @Path("/notfound")
    @GET
    Response notfound() {
        Response.status(404).build();
    }

    public static class SampleModel extends TemplateView {
        String name
    }
}
