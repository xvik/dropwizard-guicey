package ru.vyarus.dropwizard.guice.support.feature

import javax.ws.rs.ext.ContextResolver
import javax.ws.rs.ext.Provider

/**
 * Check other provider type installation.
 *
 * @author Vyacheslav Rusakov 
 * @since 14.10.2014
 */
@Provider
class DummyOtherProvider implements ContextResolver{

    @Override
    Object getContext(Class type) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
