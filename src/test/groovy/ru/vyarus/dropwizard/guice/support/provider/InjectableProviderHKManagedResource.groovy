package ru.vyarus.dropwizard.guice.support.provider

import com.google.common.base.Preconditions
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.HK2Managed
import ru.vyarus.dropwizard.guice.support.feature.CustomFeature
import ru.vyarus.dropwizard.guice.support.provider.annotated.Auth
import ru.vyarus.dropwizard.guice.support.provider.annotated.User

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.Context
import javax.ws.rs.core.Response

/**
 * @author Vyacheslav Rusakov 
 * @since 25.11.2014
 */
@Path("/prototype")
@Produces("application/json")
@HK2Managed
@javax.inject.Singleton
class InjectableProviderHKManagedResource {

    @GET
    @Path("/")
    public Response latest(@Context Locale locale, @Context CustomFeature feature, @Auth User user) {
        Preconditions.checkNotNull(locale)
        Preconditions.checkNotNull(feature)
        Preconditions.checkNotNull(user)
        return Response.ok().build();
    }
}

