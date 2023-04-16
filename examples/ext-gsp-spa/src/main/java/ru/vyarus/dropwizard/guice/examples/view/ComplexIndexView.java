package ru.vyarus.dropwizard.guice.examples.view;

import ru.vyarus.guicey.gsp.views.template.Template;
import ru.vyarus.guicey.gsp.views.template.TemplateView;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

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
