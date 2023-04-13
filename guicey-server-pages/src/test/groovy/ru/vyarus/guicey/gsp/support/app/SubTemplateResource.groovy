package ru.vyarus.guicey.gsp.support.app

import ru.vyarus.guicey.gsp.views.template.Template
import ru.vyarus.guicey.gsp.views.template.TemplateView

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path

/**
 * @author Vyacheslav Rusakov
 * @since 02.12.2019
 */
@Path("/sub/")
@Template("/app/template.ftl")
class SubTemplateResource {

    @Path("/sample")
    @GET
    TemplateView getSub() {
        return new TemplateView()
    }
}
