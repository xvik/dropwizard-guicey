package ru.vyarus.guicey.gsp.support.app

import ru.vyarus.guicey.gsp.views.template.Template
import ru.vyarus.guicey.gsp.views.template.TemplateView

import javax.ws.rs.GET
import javax.ws.rs.Path

/**
 * @author Vyacheslav Rusakov
 * @since 02.12.2019
 */
@Path("/app/")
@Template("/app/template.ftl")
class OverridableTemplateResource {

    @Path("/sub/{name}")
    @GET
    TemplateView getSub() {
        return new TemplateView()
    }

    @Path("/sample")
    @GET
    TemplateView getSample() {
        return new TemplateView()
    }
}
