package ru.vyarus.dropwizard.guice.test.jupiter.setup.rest;

import com.google.inject.Inject;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.AbstractPlatformTest;
import ru.vyarus.dropwizard.guice.debug.report.jersey.JerseyConfig;
import ru.vyarus.dropwizard.guice.debug.report.jersey.JerseyConfigRenderer;
import ru.vyarus.dropwizard.guice.module.context.option.Options;
import ru.vyarus.dropwizard.guice.module.installer.InstallersOptions;
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.rest.RestClient;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest;
import ru.vyarus.dropwizard.guice.test.rest.support.RestStubApp;

/**
 * Important because dropwizard extensions registered manually (same as in dropwizard test:
 * io.dropwizard.testing.common.DropwizardTestResourceConfig)! But they must be in sync with server implementation.
 *
 * @author Vyacheslav Rusakov
 * @since 24.02.2025
 */
public class SameDwExtensionsInStubTest extends AbstractPlatformTest {

    static String dwReport;
    static String stubReport;

    @Test
    void testSameExtensions() {

        run(TestDw.class);
        run(TestStub.class);

        Assertions.assertEquals(dwReport, stubReport);
    }

    @TestDropwizardApp(RestStubApp.class)
    @Disabled
    public static class TestDw {

        @Inject
        Options options;
        @Inject
        InjectionManager injectionManager;

        @Test
        void test() {
            final Boolean guiceFirstMode = options.get(InstallersOptions.JerseyExtensionsManagedByGuice);
            dwReport = new JerseyConfigRenderer(injectionManager, guiceFirstMode)
                    .renderReport(new JerseyConfig());
        }
    }

    @TestGuiceyApp(RestStubApp.class)
    @Disabled
    public static class TestStub {

        @StubRest
        RestClient rest;

        @Inject
        Options options;
        @Inject
        InjectionManager injectionManager;

        @Test
        void test() {
            final Boolean guiceFirstMode = options.get(InstallersOptions.JerseyExtensionsManagedByGuice);
            stubReport = new JerseyConfigRenderer(injectionManager, guiceFirstMode)
                    .renderReport(new JerseyConfig());
        }
    }

    @Override
    protected String clean(String out) {
        return out;
    }
}
