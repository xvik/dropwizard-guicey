package ru.vyarus.dropwizard.guice.config.option.mapper

import org.junit.jupiter.api.extension.ExtendWith
import ru.vyarus.dropwizard.guice.module.context.option.Option
import ru.vyarus.dropwizard.guice.module.context.option.mapper.OptionsMapper
import spock.lang.Specification
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables
import uk.org.webcompere.systemstubs.jupiter.SystemStub
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension
import uk.org.webcompere.systemstubs.properties.SystemProperties
import uk.org.webcompere.systemstubs.stream.SystemOut

import java.util.function.Function

/**
 * @author Vyacheslav Rusakov
 * @since 27.04.2018
 */
@ExtendWith(SystemStubsExtension)
class OptionsMappingTest extends Specification {

    @SystemStub
    EnvironmentVariables ENV
    @SystemStub
    SystemOut out
    @SystemStub
    SystemProperties propsReset

    def "Check variables mapping"() {

        setup:
        ENV.set("VAR", "1")
        System.setProperty("foo", "bar")

        when: "mapping properties"
        def res = new OptionsMapper()
                .printMappings()
                .env("VAR", Opts.OptInt)
                .env("VAR2", Opts.OptDbl)
                .prop("foo", Opts.OptStr)
                .prop("foo2", Opts.OptShort)
                .string(Opts.OptBool, "true")
                .map()
        then: "only existing mapped"
        res[Opts.OptInt] == 1
        res[Opts.OptDbl] == null
        res[Opts.OptStr] == 'bar'
        res[Opts.OptShort] == null
        res[Opts.OptBool] == true

        and: "log ok"
        out.text.replace("\r", "") == """\tenv: VAR                   Opts.OptInt = 1
\tprop: foo                  Opts.OptStr = bar
\t                           Opts.OptBool = true
"""

        when: "logging disabled"
        out.clear()
        res = new OptionsMapper()
                .env("VAR", Opts.OptInt)
                .map()
        then: "no log"
        res[Opts.OptInt] == 1
        out.text == ""
    }

    def "Check mass mapping"() {

        setup:
        System.setProperty("option.${Opts.name}.${Opts.OptBool.name()}", "true")
        System.setProperty("option.${Opts.name}.${Opts.OptInt.name()}", "1")
        System.setProperty("option.${Opts.name}.${Opts.OptDbl.name()}", "original")

        when: "mass mapping props"
        def res = new OptionsMapper()
        // override option mapping
                .prop("option.${Opts.name}.${Opts.OptDbl.name()}", Opts.OptDbl, { val -> 12 } as Function)
                .props()
                .map()
        then: "custom option overridden by manual mapping"
        res[Opts.OptDbl] == 12
        res[Opts.OptBool] == true
        res[Opts.OptInt] == 1
    }

    static enum Opts implements Option {
        OptStr(String, null),
        OptBool(Boolean, null),
        OptInt(Integer, null),
        OptDbl(Double, null),
        OptShort(Short, null),
        OptBt(Byte, null);

        private Class<?> type;
        private Object value;

        Opts(final Class type, final Object value) {
            this.type = type;
            this.value = value;
        }

        @Override
        Class getType() {
            return type
        }

        @Override
        Object getDefaultValue() {
            return defaultValue
        }
    }
}
