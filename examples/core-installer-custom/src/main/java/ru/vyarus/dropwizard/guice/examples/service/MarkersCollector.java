package ru.vyarus.dropwizard.guice.examples.service;

import ru.vyarus.dropwizard.guice.examples.installer.Marker;

import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Vyacheslav Rusakov
 * @since 31.12.2019
 */
@Singleton
public class MarkersCollector {

    private final List<Marker> markers = new ArrayList<>();

    public void register(Marker marker) {
        markers.add(marker);
    }

    public List<Marker> getMarkers() {
        return markers;
    }
}
