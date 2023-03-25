package ru.vyarus.guicey.gsp.support.app.clash

import ru.vyarus.guicey.gsp.views.template.Template
import ru.vyarus.guicey.gsp.views.template.TemplateView

import javax.ws.rs.GET
import javax.ws.rs.Path

/**
 * @author Vyacheslav Rusakov
 * @since 05.12.2019
 */
@Path("/foo/")
@Template("/app/template.ftl")
class BaseViewResource {

    @Path("/one")
    @GET
    TemplateView getSub1() {
        return new TemplateView()
    }

    @Path("/bar/two")
    @GET
    TemplateView getSub2() {
        return new TemplateView()
    }
}
