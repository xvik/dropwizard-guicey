package ru.vyarus.dropwizard.guice.test.jupiter.setup.spy


import javax.inject.Inject
import org.mockito.Mockito
import org.mockito.internal.util.MockUtil
import ru.vyarus.dropwizard.guice.support.DefaultTestApp
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import ru.vyarus.dropwizard.guice.test.jupiter.ext.spy.SpyBean
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 26.03.2025
 */
@TestGuiceyApp(value = DefaultTestApp, debug = true)
class SpySpockTest extends Specification {

    // could be used only with JAVA classes
    @SpyBean
    SpySimpleTest.Service1 spy1
    @SpyBean
    static SpySimpleTest.Service2 spy2


    @Inject
    SpySimpleTest.OuterService outerService

    void setupSpec() {
        assert spy2
    }

    void setup() {
        assert spy1
        assert spy2
    }

    def "Check spy"() {

        expect:
        spy1
        spy2
        MockUtil.isSpy(spy1)
        MockUtil.isSpy(spy2)
        "Hello 11 Hello 11" == outerService.doSomething(11)

        when:
        Mockito.verify(spy1, Mockito.times(1)).get(11)
        Mockito.verify(spy2, Mockito.times(1)).get(11)

        then:
        true
    }
}
