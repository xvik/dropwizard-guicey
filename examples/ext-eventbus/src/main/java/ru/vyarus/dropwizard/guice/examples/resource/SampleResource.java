package ru.vyarus.dropwizard.guice.examples.resource;

import com.google.common.eventbus.EventBus;
import ru.vyarus.dropwizard.guice.examples.event.BarEvent;
import ru.vyarus.dropwizard.guice.examples.event.FooEvent;
import ru.vyarus.dropwizard.guice.examples.service.EventListener;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author Vyacheslav Rusakov
 * @since 07.03.2017
 */
@Path("/sample")
@Produces(MediaType.APPLICATION_JSON)
public class SampleResource {

    @Inject
    private EventBus bus;
    @Inject
    private EventListener listener;

    @GET
    @Path("/foo")
    public int foo() {
        bus.post(new FooEvent());
        return listener.getFooCalls();
    }


    @GET
    @Path("/bar")
    public int bar() {
        bus.post(new BarEvent());
        return listener.getBarCalls();
    }


    @GET
    @Path("/stats")
    @Produces(MediaType.TEXT_PLAIN)
    public String base() {
        return String.format("Foo: %s, Bar: %s, Base: %s",
                listener.getFooCalls(), listener.getBarCalls(), listener.getBaseCalls());
    }
}
