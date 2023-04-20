package ru.vyarus.dropwizard.guice.examples.rest;

import ru.vyarus.dropwizard.guice.examples.validator.CustomCondition;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;

/**
 * Dropwizard performs method validations.
 *
 * @author Vyacheslav Rusakov
 * @since 12.01.2018
 */
@Path("/val")
@Produces("application/json")
public class ValResource {


    // simple validation
    @GET
    @Path("/q")
    public String doStaff(@NotNull @QueryParam("q") String something) {
        return "done";
    }

    // validation with custom guice-aware validator
    @GET
    @Path("/custom")
    public String withCustomValidator(@CustomCondition @QueryParam("q") String something) {
        return "done";
    }
}
