package ru.vyarus.dropwizard.guice.config.option.mapper

import ru.vyarus.dropwizard.guice.module.context.option.Option
import ru.vyarus.dropwizard.guice.module.context.option.mapper.OptionParser
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 27.04.2018
 */
class OptionParserTest extends Specification {

    def "Check option recognition"() {

        when: "parse option"
        Opts res = OptionParser.recognizeOption('ru.vyarus.dropwizard.guice.config.option.mapper.OptionParserTest$Opts.OptStr')
        then: "recognized"
        res == Opts.OptStr

        when: "parse not enum"
        OptionParser.recognizeOption('ru.vyarus.dropwizard.guice.config.option.mapper.OptionParserTest$AlsoNotOpt.someth')
        then: "err"
        def ex = thrown(IllegalStateException)
        ex.message.startsWith("Failed to convert value")

        when: "parse enum not option"
        OptionParser.recognizeOption('ru.vyarus.dropwizard.guice.config.option.mapper.OptionParserTest$NotOption.Opt1')
        then: "err"
        ex = thrown(IllegalStateException)
        ex.message.contains("is not an option type")

        when: "parse unknown option"
        OptionParser.recognizeOption('ru.vyarus.dropwizard.guice.config.option.mapper.OptionParserTest$Opts.OptUnknow')
        then: "err"
        ex = thrown(IllegalStateException)
        ex.message.startsWith("Failed to convert value")
    }

    def "Check value parse"() {

        when: "parse string value"
        def res = OptionParser.parseValue(Opts.OptStr, "ds")
        then: "ok"
        res == 'ds'

        when: "parse bool value"
        res = OptionParser.parseValue(Opts.OptBool, "tRuE")
        then: "ok"
        res == true

        when: "parse int value"
        res = OptionParser.parseValue(Opts.OptInt, "-11")
        then: "ok"
        res == -11

        when: "parse dbl value"
        res = OptionParser.parseValue(Opts.OptDbl, "-11.1")
        then: "ok"
        res == -11.1

        when: "parse short value"
        res = OptionParser.parseValue(Opts.OptShort, "1")
        then: "ok"
        res == 1

        when: "parse byte value"
        res = OptionParser.parseValue(Opts.OptBt, "1")
        then: "ok"
        res == 1

        when: "unsupported type"
        OptionParser.parseValue(Opts.Uns, "1")
        then: "err"
        def ex = thrown(IllegalStateException)
        ex.message.startsWith("Failed to convert ")

        when: "bad value"
        OptionParser.parseValue(Opts.OptInt, "dfsdf")
        then: "err"
        ex = thrown(IllegalStateException)
        ex.message.startsWith("Failed to convert ")

        when: "empty value"
        OptionParser.parseValue(Opts.OptInt, "")
        then: "err"
        ex = thrown(IllegalStateException)
        ex.message.startsWith("Empty value is not allowed for option")
    }

    static enum Opts implements Option {
        OptStr(String, null),
        OptBool(Boolean, null),
        OptInt(Integer, null),
        OptDbl(Double, null),
        OptShort(Short, null),
        OptBt(Byte, null),
        Uns(Object.class, null);

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

    static enum NotOption {
        Opt1
    }

    static class AlsoNotOpt implements Option {
        @Override
        Class getType() {
            return null
        }

        @Override
        Object getDefaultValue() {
            return null
        }
    }
}
