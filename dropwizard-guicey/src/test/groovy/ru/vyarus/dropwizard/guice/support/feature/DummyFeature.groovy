package ru.vyarus.dropwizard.guice.support.feature

import jakarta.ws.rs.core.Feature
import jakarta.ws.rs.core.FeatureContext

/**
 * @author Vyacheslav Rusakov
 * @since 16.01.2016
 */
class DummyFeature implements Feature {

    @Override
    boolean configure(FeatureContext context) {
        return false
    }
}
