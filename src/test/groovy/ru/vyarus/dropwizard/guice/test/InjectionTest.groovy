package ru.vyarus.dropwizard.guice.test

import com.google.inject.Inject
import com.google.inject.Singleton
import ru.vyarus.dropwizard.guice.support.AutoScanApplication
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import spock.lang.Shared
import spock.lang.Specification


/**
 * @author Vyacheslav Rusakov 
 * @since 03.01.2015
 */
@TestGuiceyApp(AutoScanApplication)
class InjectionTest extends Specification {

    // shared field can't be initialized!
    @Shared @Inject TestBean sharedBean

    // new instance injected on each test
    @Inject TestBean bean

    // the same context used for all tests (in class), so the same bean instance inserted before each test
    @Inject TestSingletonBean singletonBean

    def "Check injection types"() {
        when: "changing state of injected beans"
        bean.value = 5
        singletonBean.value = 15

        then: "instances are different"
        sharedBean == null
        bean.value == 5
        singletonBean.value == 15

    }

    def "Check shared state"() {

        expect: "shared bean instance is the same, whereas other one re-injected"
        sharedBean == null
        bean.value == 0
        singletonBean.value == 15 // the same instance was set before second test
    }

    // bean is in prototype scope
    static class TestBean {
        int value
    }

    @Singleton
    static class TestSingletonBean {
        int value
    }
}