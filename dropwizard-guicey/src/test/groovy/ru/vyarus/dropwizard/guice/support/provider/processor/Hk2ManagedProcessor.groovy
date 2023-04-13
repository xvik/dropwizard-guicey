package ru.vyarus.dropwizard.guice.support.provider.processor

import org.glassfish.jersey.server.model.ModelProcessor
import org.glassfish.jersey.server.model.ResourceModel
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.JerseyManaged

import jakarta.ws.rs.core.Configuration

/**
 * @author Vyacheslav Rusakov
 * @since 05.12.2022
 */
@JerseyManaged
class Hk2ManagedProcessor implements ModelProcessor {

    @Override
    ResourceModel processResourceModel(ResourceModel resourceModel, Configuration configuration) {
        return resourceModel
    }

    @Override
    ResourceModel processSubResource(ResourceModel subResourceModel, Configuration configuration) {
        return subResourceModel
    }
}
