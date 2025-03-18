package ru.vyarus.dropwizard.guice.bundles.transitive

import com.google.inject.Inject
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.installer.CoreInstallersBundle
import ru.vyarus.dropwizard.guice.module.installer.WebInstallersBundle
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyEnvironment
import ru.vyarus.dropwizard.guice.module.jersey.debug.HK2DebugBundle
import ru.vyarus.dropwizard.guice.support.DefaultTestApp
import ru.vyarus.dropwizard.guice.support.util.GuiceRestrictedConfigBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp

/**
 * @author Vyacheslav Rusakov
 * @since 18.03.2025
 */
@TestGuiceyApp(App)
class TransitiveBundlesInitTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info

    def "Check transitive installation order"() {

        expect:
        initOrder == ["Last", "Middle", "Root"]
        runOrder == ["Last", "Middle", "Root"]
        info.getGuiceyBundlesInInitOrder() == [Last, Middle, Root, HK2DebugBundle, GuiceRestrictedConfigBundle, WebInstallersBundle, CoreInstallersBundle]
    }

    static List<String> initOrder = new ArrayList<>()
    static List<String> runOrder = new ArrayList<>()

    static class App extends DefaultTestApp {
        @Override
        protected GuiceBundle configure() {
            return GuiceBundle.builder()
                    .bundles(new Root())
                    .build()
        }
    }

    static class Root implements GuiceyBundle {
        @Override
        void initialize(GuiceyBootstrap bootstrap) {
            bootstrap.bundles(new Middle())
            initOrder.add("Root")
        }

        @Override
        void run(GuiceyEnvironment environment) throws Exception {
            runOrder.add("Root")
        }
    }

    static class Middle implements GuiceyBundle {
        @Override
        void initialize(GuiceyBootstrap bootstrap) {
            bootstrap.bundles(new Last())
            initOrder.add("Middle")
        }

        @Override
        void run(GuiceyEnvironment environment) throws Exception {
            runOrder.add("Middle")
        }
    }

    static class Last implements GuiceyBundle {

        @Override
        void initialize(GuiceyBootstrap bootstrap) {
            initOrder.add("Last")
        }

        @Override
        void run(GuiceyEnvironment environment) throws Exception {
            runOrder.add("Last")
        }
    }
}
