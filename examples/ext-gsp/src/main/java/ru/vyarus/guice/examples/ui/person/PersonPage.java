package ru.vyarus.guice.examples.ui.person;

import ru.vyarus.guice.examples.dao.PersonDao;
import ru.vyarus.guicey.gsp.views.template.Template;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author Vyacheslav Rusakov
 * @since 23.10.2019
 */
// Path starts with application name
@Path("/app/person/")
@Produces(MediaType.TEXT_HTML)
// Important marker
@Template
public class PersonPage {

    @Inject
    private PersonDao dao;

    @GET
    @Path("/")
    public PersonView getMaster() {
        return new PersonView(dao.find(1));
    }

    @GET
    @Path("/{id}")
    public PersonView getPerson(@PathParam("id") Integer id) {
        return new PersonView(dao.find(id));
    }
}
