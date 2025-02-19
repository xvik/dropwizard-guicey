package ru.vyarus.dropwizard.guice.test.jupiter.setup

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import ru.vyarus.dropwizard.guice.AbstractPlatformTest
import ru.vyarus.dropwizard.guice.support.DefaultTestApp
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import ru.vyarus.dropwizard.guice.test.jupiter.ext.TestDropwizardAppExtension
import ru.vyarus.dropwizard.guice.test.jupiter.ext.TestGuiceyAppExtension

/**
 * @author Vyacheslav Rusakov
 * @since 19.02.2025
 */

class ExtensionsDisableTest extends AbstractPlatformTest {

    @Test
    void testGuiceyAnn() {
        
        String res = run(TestGuiceyAnn)
        Assertions.assertThat(res).doesNotContain("StubsSupport")
    }

    @Test
    void testDwAnn() {

        String res = run(TestDwAnn)
        Assertions.assertThat(res).doesNotContain("StubsSupport")
    }

    @Test
    void testGuiceyExt() {

        String res = run(TestGuiceyExt)
        Assertions.assertThat(res).doesNotContain("StubsSupport")
    }

    @Test
    void testDwExt() {

        String res = run(TestDwExt)
        Assertions.assertThat(res).doesNotContain("StubsSupport")
    }

    @TestGuiceyApp(value = DefaultTestApp, useDefaultExtensions = false, debug = true)
    @Disabled
    static class TestGuiceyAnn {

        @Test
        void test() {
            // nothing
        }
    }

    @TestDropwizardApp(value = DefaultTestApp, useDefaultExtensions = false, debug = true)
    @Disabled
    static class TestDwAnn {

        @Test
        void test() {
            // nothing
        }
    }

    @Disabled
    static class TestGuiceyExt {

        static TestGuiceyAppExtension ext = TestGuiceyAppExtension.forApp(DefaultTestApp)
                .disableDefaultExtensions().debug().create()

        @Test
        void test() {
            // nothing
        }
    }

    @Disabled
    static class TestDwExt {

        static TestDropwizardAppExtension ext = TestDropwizardAppExtension.forApp(DefaultTestApp)
                .disableDefaultExtensions().debug().create()

        @Test
        void test() {
            // nothing
        }
    }

    @Override
    protected String clean(String out) {
        return out
    }
}
