package ru.vyarus.dropwizard.guice.examples

import ru.vyarus.dropwizard.guice.examples.job.SampleJob
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import spock.lang.Specification

import jakarta.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 11.03.2018
 */
@TestGuiceyApp(JobsApplication)
class JobsAppTest extends Specification {

    @Inject
    SampleJob job

    def "Check task execution"() {

        expect: "task called"
        job.iDidIt
    }
}
