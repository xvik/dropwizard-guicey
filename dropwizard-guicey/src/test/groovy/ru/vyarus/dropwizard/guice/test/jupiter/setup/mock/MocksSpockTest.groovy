package ru.vyarus.dropwizard.guice.test.jupiter.setup.mock


import com.google.inject.Inject
import org.mockito.Mockito
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.support.DefaultTestApp
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import ru.vyarus.dropwizard.guice.test.jupiter.ext.mock.MockBean

/**
 * @author Vyacheslav Rusakov
 * @since 26.03.2025
 */
@TestGuiceyApp(value = DefaultTestApp, debug = true)
class MocksSpockTest extends AbstractTest {

    @Inject
    Service1 service1

    @Inject
    Service2 service2

    @MockBean
    Service1 mock1

    @MockBean
    static Service2 mock2

    void setupSpec() {
        assert mock2
    }

    void setup() {
        assert mock1
        assert mock2

        Mockito.when(mock1.foo()).thenReturn("bar1")
        Mockito.when(mock2.foo()).thenReturn("bar2")
    }

    def "Check mock execution"() {

        expect:
        service1 == mock1
        service2 == mock2
        "bar1" == service1.foo()
        "bar2" == service2.foo()
    }

    static class Service1 {
        String foo() {
            return "foo1"
        }
    }

    static class Service2 {
        String foo() {
            return "foo2"
        }
    }
}
