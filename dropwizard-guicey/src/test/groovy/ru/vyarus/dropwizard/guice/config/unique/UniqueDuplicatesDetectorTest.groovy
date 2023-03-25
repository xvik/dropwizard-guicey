package ru.vyarus.dropwizard.guice.config.unique

import ru.vyarus.dropwizard.guice.module.context.unique.UniqueItemsDuplicatesDetector
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 28.09.2019
 */
class UniqueDuplicatesDetectorTest extends Specification {

    def "Check unique items detector behaviour"() {

        setup:
        UniqueItemsDuplicatesDetector detector = new UniqueItemsDuplicatesDetector(Something)
        def something = new Something()

        expect: "unique items deduplication"
        detector.getDuplicateItem([something, new Something()], new Something()) == something

        and: "no deduplication for others"
        detector.getDuplicateItem([new OtherThing(), new OtherThing()],  new OtherThing()) == null
    }

    def "Check incorrect initialization"() {

        when: "no classes provided"
        new UniqueItemsDuplicatesDetector()
        then: "err"
        thrown(IllegalArgumentException)
    }

    static class Something {}
    static class OtherThing {}
}
