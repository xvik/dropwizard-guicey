package ru.vyarus.dropwizard.guice.config.option

import ru.vyarus.dropwizard.guice.config.option.support.SampleOptions
import ru.vyarus.dropwizard.guice.module.context.option.internal.OptionsSupport
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 13.08.2016
 */
class OptionsSupportTest extends Specification {

    def "Check options support behaviour"() {

        OptionsSupport support = new OptionsSupport()

        when: "setting option value"
        support.set(SampleOptions.BoolFalse, true)
        then: "value set"
        support.get(SampleOptions.BoolFalse)

        then: "default value correctly returned"
        support.get(SampleOptions.BoolTrue)

        when: "changing default"
        support.set(SampleOptions.BoolTrue, false)
        then: 'value overridden'
        support.get(SampleOptions.BoolTrue) == false

        then: "getting used options"
        support.getOptions() == [SampleOptions.BoolFalse, SampleOptions.BoolTrue] as Set

        then: "getting null default"
        support.get(SampleOptions.NullOption) == null
    }

    def "Check value validation"() {

        OptionsSupport support = new OptionsSupport()

        when: "setting null"
        support.set(SampleOptions.BoolFalse, null)
        then: "exception"
        thrown(NullPointerException)

        when: "setting wrong value"
        support.set(SampleOptions.BoolFalse, "wrong")
        then: "exception"
        thrown(IllegalArgumentException)
    }
}
