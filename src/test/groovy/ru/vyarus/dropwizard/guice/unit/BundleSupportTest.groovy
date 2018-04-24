package ru.vyarus.dropwizard.guice.unit

import com.google.common.collect.Lists
import io.dropwizard.Bundle
import io.dropwizard.ConfiguredBundle
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.admin.AdminRestBundle
import ru.vyarus.dropwizard.guice.module.installer.CoreInstallersBundle
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
import ru.vyarus.dropwizard.guice.module.installer.util.BundleSupport
import ru.vyarus.dropwizard.guice.module.jersey.debug.HK2DebugBundle

/**
 * @author Vyacheslav Rusakov
 * @since 19.01.2016
 */
class BundleSupportTest extends AbstractTest {

    def "Check duplicates clear"() {

        when: "prepare list with duplicates"
        List lst = BundleSupport.removeDuplicates(Lists.newArrayList(new CoreInstallersBundle(),
                new HK2DebugBundle(), new CoreInstallersBundle()))
        then: "cleared"
        lst.size() == 2
        lst[0] instanceof CoreInstallersBundle
        lst[1] instanceof HK2DebugBundle

        when: "prepare list without duplicates"
        lst = BundleSupport.removeDuplicates(Lists.newArrayList(new CoreInstallersBundle()))
        then: "not changed"
        lst.size() == 1
        lst[0] instanceof CoreInstallersBundle
    }

    def "Check dw bundles recognition"() {

        when: "prepare bootstrap with hybrid bundles"
        def bootstrap = new Bootstrap(null)
        bootstrap.addBundle(new SampleBundle())
        bootstrap.addBundle(new SampleConfiguredBundle())
        bootstrap.addBundle(new AdminRestBundle())
        def res = BundleSupport.findBundles(bootstrap, GuiceyBundle)
        then:
        res.size() == 2
        res[0] instanceof SampleBundle
        res[1] instanceof SampleConfiguredBundle

        when: "resolve bundles from wrong field"
        BundleSupport.resolveBundles(bootstrap, "dsds")
        then: "err"
        def ex = thrown(IllegalStateException)
        ex.message.startsWith("Failed to resolve bootstrap field")
    }

    def "Check filtering"() {

        setup: "prepare filter list"
        def filter = [SampleBundle, AdminRestBundle]

        when: "filtering bundles list"
        def res = BundleSupport.removeTypes([new CoreInstallersBundle(), new SampleBundle(), new HK2DebugBundle(),
                                             new SampleConfiguredBundle(), new AdminRestBundle()], filter)
        then: "filtered"
        res*.class == [CoreInstallersBundle, HK2DebugBundle, SampleConfiguredBundle]
    }

    static class SampleBundle implements Bundle, GuiceyBundle {
        @Override
        void initialize(Bootstrap<?> bootstrap) {
        }

        @Override
        void run(Environment environment) {
        }

        @Override
        void initialize(GuiceyBootstrap bootstrap) {
        }
    }

    static class SampleConfiguredBundle implements ConfiguredBundle, GuiceyBundle {
        @Override
        void run(Object configuration, Environment environment) throws Exception {
        }

        @Override
        void initialize(Bootstrap bootstrap) {
        }

        @Override
        void initialize(GuiceyBootstrap bootstrap) {
        }
    }
}