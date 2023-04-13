package ru.vyarus.guicey.gsp.support.relative

import ru.vyarus.guicey.gsp.views.template.Template
import ru.vyarus.guicey.gsp.views.template.TemplateView

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path

/**
 * @author Vyacheslav Rusakov
 * @since 27.01.2019
 */
@Path("/app/relative/")
// template declaration relative to class
@Template("relative.ftl")
class RelativeTemplateResource {

    @Path("/direct")
    @GET
    TemplateView get() {
        return new TemplateView()
    }


    @Path("/relative")
    @GET
    TemplateView getRelative() {
        // relative template name also resolved relative to resource class
        return new TemplateView("../root.ftl")
    }

    @Path("/dir")
    @GET
    TemplateView getDir() {
        // relative template to classpath resources dir
        return new TemplateView("template.ftl")
    }
}
