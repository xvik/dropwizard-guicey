package ru.vyarus.dropwizard.guice.cases.hkscope.support

import javax.ws.rs.ext.ContextResolver
import javax.ws.rs.ext.Provider

/**
 * @author Vyacheslav Rusakov
 * @since 19.01.2016
 */
@Provider
class GuiceContextResolver implements ContextResolver<Context> {

    @Override
    Context getContext(Class type) {
        return null
    }

    static class Context {}
}
