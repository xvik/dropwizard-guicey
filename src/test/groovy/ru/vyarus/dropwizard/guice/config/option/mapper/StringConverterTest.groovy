package ru.vyarus.dropwizard.guice.config.option.mapper

import ru.vyarus.dropwizard.guice.GuiceyOptions
import ru.vyarus.dropwizard.guice.module.context.option.Option
import ru.vyarus.dropwizard.guice.module.context.option.mapper.StringConverter
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 27.04.2018
 */
class StringConverterTest extends Specification {


    def "Check simple conversions"() {

        expect:
        StringConverter.convert(String, 'sample') == 'sample'
        StringConverter.convert(Integer, '1') == 1
        StringConverter.convert(Double, '-1.1') == -1.1
        StringConverter.convert(Short, '1') == 1
        StringConverter.convert(Byte, '1') == 1
        StringConverter.convert(GuiceyOptions, GuiceyOptions.ConfigureFromDropwizardBundles.name()) == GuiceyOptions.ConfigureFromDropwizardBundles
        StringConverter.convert(Enum.class, GuiceyOptions.getName() + "." + GuiceyOptions.ConfigureFromDropwizardBundles.name()) == GuiceyOptions.ConfigureFromDropwizardBundles
    }

    def "Check collections conversions"() {

        expect:
        StringConverter.convert(String[], 'sam,ple,gg') == ['sam', 'ple', 'gg'] as String[]
        StringConverter.convert(Integer[], '1,2,3') == [1, 2, 3] as Integer[]
        StringConverter.convert(EnumSet, [GuiceyOptions.getName() + "." + GuiceyOptions.ConfigureFromDropwizardBundles.name(),
                                          GuiceyOptions.getName() + "." + GuiceyOptions.GuiceFilterRegistration.name()].join(',')) == EnumSet.of(
                GuiceyOptions.ConfigureFromDropwizardBundles, GuiceyOptions.GuiceFilterRegistration)

    }

    def "Check fail cases"() {

        when: "converting to unknown class"
        StringConverter.convert(File, "dfd")
        then:
        def ex = thrown(IllegalStateException)
        ex.message.startsWith('Can\'t convert value')

        when: "conversion fails"
        StringConverter.convert(Integer, "dfd")
        then:
        ex = thrown(IllegalStateException)
        ex.message.startsWith('Failed to convert value')

        when: "erro parsing array"
        StringConverter.convert(Integer[], "1,er,2")
        then:
        ex = thrown(IllegalStateException)
        ex.message.startsWith('Failed to parse array')

        when: 'bad enum set'
        StringConverter.convert(EnumSet, "1,er,2")
        then:
        ex = thrown(IllegalStateException)
        ex.message.startsWith('Failed to parse EnumSet from')

        when: "enum type not enum"
        StringConverter.convert(Enum, Option.getName() + ".dfdf")
        then:
        ex = thrown(IllegalStateException)
        ex.cause.cause.message.startsWith("Type ${Option.name} is not enum")

        when: "bad enum constant"
        StringConverter.convert(Enum, GuiceyOptions.getName() + ".dfdf")
        then:
        ex = thrown(IllegalStateException)
        ex.cause.message.startsWith("Failed to recognize enum value:")
    }
}