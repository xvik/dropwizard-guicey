package ru.vyarus.dropwizard.guice.test.jupiter.setup.stub

import javax.inject.Inject
import javax.inject.Singleton
import ru.vyarus.dropwizard.guice.support.DefaultTestApp
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import ru.vyarus.dropwizard.guice.test.jupiter.ext.stub.StubBean
import ru.vyarus.dropwizard.guice.test.stub.StubLifecycle
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 26.03.2025
 */
@TestGuiceyApp(value = DefaultTestApp, debug = true)
class StubsSpockTest extends Specification {


    @Inject
    Service1 service1

    @Inject
    Service2 service2

    @Inject
    Service service

    @StubBean(Service1)
    Service1Stub stub

    @StubBean(Service2)
    static Service2Stub stub2

    void setupSpec() {
        assert stub2
    }

    void setup() {
        assert stub
        assert stub2
    }

    def "Check stubs"() {

        expect:
        service1 == stub
        service2 == stub2
        "moon" == service.get()
        stub.beforeCalled
        !stub.afterCalled
    }

    static class Service1Stub extends Service1 implements StubLifecycle {

        static boolean created

        public boolean beforeCalled
        public boolean afterCalled

        Service1Stub() {
            assert !created
            created = true
        }

        @Override
        String get() {
            return "moon"
        }

        @Override
        void before() {
            beforeCalled = true
        }

        @Override
        void after() {
            afterCalled = true
        }
    }

    static class Service2Stub extends Service2 {}

    static class Service1 {

        String get() {
            return "sun"
        }
    }

    static class Service2 {
    }

    @Singleton
    static class Service {

        @Inject
        Service1 service1

        String get() {
            return service1.get()
        }
    }
}
