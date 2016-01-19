package ru.vyarus.dropwizard.guice.cases.hkscope.support

import javax.ws.rs.container.DynamicFeature
import javax.ws.rs.container.ResourceInfo
import javax.ws.rs.core.FeatureContext
import javax.ws.rs.ext.Provider

/**
 * @author Vyacheslav Rusakov
 * @since 19.01.2016
 */
@Provider
class GuiceDynamicFeature implements DynamicFeature {

    @Override
    void configure(ResourceInfo resourceInfo, FeatureContext context) {

    }
}
