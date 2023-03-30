package ru.vyarus.dropwizard.guice.examples.view;

import ru.vyarus.guicey.gsp.views.template.Template;
import ru.vyarus.guicey.gsp.views.template.TemplateView;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author Vyacheslav Rusakov
 * @since 22.10.2020
 */
@Path("/views/app3/index/")
@Produces(MediaType.TEXT_HTML)
// otherwise template could be specified in Model constructor (super)
// and in this case empty @Template marker would still be required
@Template("index3.ftl")
public class ComplexIndexView {

    @GET
    public Model index() {
        // just an example of computed model property
        return new Model("sample string");
    }

    public static class Model extends TemplateView {
        String sample;

        public Model(String sample) {
            this.sample = sample;
        }

        public String getSample() {
            return sample;
        }
    }
}
