package ru.vyarus.dropwizard.guice.config.option

import ru.vyarus.dropwizard.guice.config.option.support.SampleOptions
import ru.vyarus.dropwizard.guice.module.context.option.internal.OptionHolder
import spock.lang.Specification


/**
 * @author Vyacheslav Rusakov
 * @since 13.08.2016
 */
class OptionHolderTest extends Specification {

    def "Check holder"() {

        when: "creating holder for option"
        OptionHolder holder = new OptionHolder(SampleOptions.BoolFalse)
        then: "holder state valid"
        !holder.set
        !holder.used
        holder.toString() == "BoolFalse = false"

        when: "getting option value"
        def val = holder.getValue()
        then: "value correct, read tracked"
        val == false
        holder.used

        when: "setting value"
        holder.setValue(true)
        then: "value set, definition tracked"
        holder.getValue() == true
        holder.set
        holder.used
    }
}