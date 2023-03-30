package ru.vyarus.dropwizard.guice.examples.service;

import ru.vyarus.dropwizard.guice.examples.installer.Marker;
import ru.vyarus.dropwizard.guice.examples.installer.MarkersInstaller;

import javax.inject.Singleton;

/**
 * Service which must be recognized and installed by
 * {@link MarkersInstaller}.
 *
 * @author Vyacheslav Rusakov
 * @since 29.01.2016
 */
@Singleton
public class SampleMarker implements Marker {
}
