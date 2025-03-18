package ru.vyarus.dropwizard.guice.bundles.transitive

import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
import ru.vyarus.dropwizard.guice.support.DefaultTestApp
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp

/**
 * @author Vyacheslav Rusakov
 * @since 18.03.2025
 */
@TestGuiceyApp(App)
class TransitiveBundlesInitTest extends AbstractTest {

    def "Check transitive installation order"() {

        expect:
        order == ["Last", "Middle", "Root"]
    }

    static List<String> order = new ArrayList<>()

    static class App extends DefaultTestApp {
        @Override
        protected GuiceBundle configure() {
            return GuiceBundle.builder().bundles(new Root()).build()
        }
    }

    static class Root implements GuiceyBundle {
        @Override
        void initialize(GuiceyBootstrap bootstrap) {
            bootstrap.bundles(new Middle())
            order.add("Root")
        }
    }

    static class Middle implements GuiceyBundle {
        @Override
        void initialize(GuiceyBootstrap bootstrap) {
            bootstrap.bundles(new Last())
            order.add("Middle")
        }
    }

    static class Last implements GuiceyBundle {

        @Override
        void initialize(GuiceyBootstrap bootstrap) {
            order.add("Last")
        }
    }
}
