package ru.vyarus.dropwizard.guice.test.client.builder.track.impl;

import ru.vyarus.dropwizard.guice.test.client.builder.track.RequestTracker;

/**
 * Identifies request objects, containing tracker. See
 * {@link RequestTracker#lookupTracker(javax.ws.rs.client.WebTarget)} and
 * {@link RequestTracker#lookupTracker(javax.ws.rs.client.Invocation.Builder)}.
 *
 * @author Vyacheslav Rusakov
 * @since 07.10.2025
 */
public interface TrackableData {

    /**
     * @return tracker object
     */
    RequestTracker getTracker();
}
