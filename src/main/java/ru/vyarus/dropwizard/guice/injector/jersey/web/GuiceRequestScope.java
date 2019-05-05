package ru.vyarus.dropwizard.guice.injector.jersey.web;

import org.glassfish.jersey.process.internal.RequestContext;
import org.glassfish.jersey.process.internal.RequestScope;

/**
 * @author Vyacheslav Rusakov
 * @since 05.05.2019
 */
public class GuiceRequestScope extends RequestScope {

    @Override
    public RequestContext createContext() {
        return new GuiceRequestContext();
    }
}
