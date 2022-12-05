package ru.vyarus.dropwizard.guice.support.feature

import org.glassfish.jersey.server.model.ModelProcessor
import org.glassfish.jersey.server.model.ResourceModel

import javax.inject.Singleton
import javax.ws.rs.core.Configuration

/**
 * @author Vyacheslav Rusakov
 * @since 04.06.2022
 */
@Singleton
class DummyModelProcessor implements ModelProcessor {

    @Override
    ResourceModel processResourceModel(ResourceModel resourceModel, Configuration configuration) {
        return resourceModel
    }

    @Override
    ResourceModel processSubResource(ResourceModel subResourceModel, Configuration configuration) {
        return subResourceModel
    }
}
