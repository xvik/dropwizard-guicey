package ru.vyarus.guicey.annotations.lifecycle

import ru.vyarus.guicey.annotations.lifecycle.module.collector.MethodsCollector
import spock.lang.Specification

import jakarta.annotation.PostConstruct

/**
 * @author Vyacheslav Rusakov
 * @since 27.11.2018
 */
class CollectorTest extends Specification {

    def "Check duplicate events prevention"() {

        when:
        MethodsCollector collector = new MethodsCollector()
        collector.call(PostConstruct)
        collector.call(PostConstruct)
        then:
        def ex = thrown(IllegalStateException)
        ex.message == "Lifecycle @PostConstruct methods were already processed"
    }
}
