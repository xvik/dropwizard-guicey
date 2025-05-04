package ru.vyarus.dropwizard.guice.test.jupiter.setup.track

import com.google.inject.Inject
import org.mockito.Mockito
import ru.vyarus.dropwizard.guice.support.DefaultTestApp
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import ru.vyarus.dropwizard.guice.test.track.MethodTrack
import ru.vyarus.dropwizard.guice.test.jupiter.ext.track.TrackBean
import ru.vyarus.dropwizard.guice.test.track.Tracker
import spock.lang.Specification

import static org.mockito.Mockito.when

/**
 * @author Vyacheslav Rusakov
 * @since 26.03.2025
 */
@TestGuiceyApp(value = DefaultTestApp, debug = true)
class TrackerSpockTest extends Specification {

    // use java classes because too much "garbage" methods would be intercepted fpr groovy objects
    @Inject
    TrackerSimpleTest.Service service

    @TrackBean(trace = true)
    Tracker<TrackerSimpleTest.Service> serviceTracker

    def "Check tracker"() {

        expect:
        serviceTracker

        // call service
        "1 call" == service.foo(1)

        TrackerSimpleTest.Service == serviceTracker.getType()
        1 == serviceTracker.size()
        1 == serviceTracker.getTracks().size()
        !serviceTracker.isEmpty()

        when:
        MethodTrack track = serviceTracker.getLastTrack()
        then:
        track.toString().contains("foo(1) = \"1 call\"")
        new Object[]{1} == track.getRawArguments()
        new String[]{"1"} == track.getArguments()
        "1 call" == track.getRawResult()
        "1 call" == track.getResult()
        "foo" == track.getMethod().getName()
        TrackerSimpleTest.Service == track.getService()
        track.getStarted() > 0
        track.getDuration()
        track.getInstanceHash()


        // call more
        "2 call" == service.foo(2)
        "1 bar" == service.bar(1)


        3 == serviceTracker.getTracks().size()

        when:
        List<MethodTrack> tracks = serviceTracker.getLastTracks(2);

        then:
        "foo(2) = \"2 call\"" == tracks.get(0).toStringTrack()
        "bar(1) = \"1 bar\"" == tracks.get(1).toStringTrack()


        // search with mockito api
        when:
        tracks = serviceTracker.findTracks { mock ->
            when(
                    mock.foo(Mockito.anyInt()))
        }

        then:
        2 == tracks.size()

        // few more calls (to check mocks correct reset)
        "foo" == tracks.get(0).getMethod().getName()
        "foo" == tracks.get(1).getMethod().getName()

        when:
        tracks = serviceTracker.findTracks { mock ->
            when(
                    mock.foo(Mockito.intThat(argument -> argument == 1)))
        }
        then:
        1 == tracks.size()
        1 == tracks.get(0).getRawArguments()[0]

        // and another call to make sure results not cached
        "1 call" == service.foo(1)

        when:
        tracks = serviceTracker.findTracks { mock ->
            when(
                    mock.foo(Mockito.intThat(argument -> argument == 1)))
        }
        then:
        2 == tracks.size()
    }

    def "Check void trace"() {
        when:
        service.baz("small")
        then:
        MethodTrack track = serviceTracker.getLastTrack()
        track.toString().contains("baz(\"small\")")
        track.getResult() == null
        track.getRawResult() == null
        track.getQuotedResult() == null
    }

    def "Check error"() {
        when:
        service.err(11)
        then:
        thrown(RuntimeException)

        MethodTrack track = serviceTracker.getLastTrack()
        track.toString().contains("err(11) ERROR IllegalStateException: error")
        track.getThrowable()
        "11" == track.getArguments()[0]
    }
}
