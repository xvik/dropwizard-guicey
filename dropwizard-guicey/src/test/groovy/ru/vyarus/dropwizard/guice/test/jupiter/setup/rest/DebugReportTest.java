package ru.vyarus.dropwizard.guice.test.jupiter.setup.rest;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.AbstractPlatformTest;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.rest.RestClient;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest;
import ru.vyarus.dropwizard.guice.test.rest.support.Resource2;
import ru.vyarus.dropwizard.guice.test.rest.support.RestFilter2;
import ru.vyarus.dropwizard.guice.test.rest.support.RestStubApp;

/**
 * @author Vyacheslav Rusakov
 * @since 26.02.2025
 */
public class DebugReportTest extends AbstractPlatformTest {

    @Test
    void testDebugReport() {

        String res = run(Test1.class);
        Assertions.assertThat(res).contains("REST stub (@StubRest) started on DebugReportTest$Test1:\n" +
                "\n" +
                "\tJersey test container factory: org.glassfish.jersey.test.inmemory.InMemoryTestContainerFactory\n" +
                "\tDropwizard exception mappers: DISABLED\n" +
                "\n" +
                "\t2 resources (disabled 1):\n" +
                "\t\tErrorResource                (r.v.d.g.t.r.support)      \n" +
                "\t\tResource1                    (r.v.d.g.t.r.support)      \n" +
                "\n" +
                "\t2 jersey extensions (disabled 1):\n" +
                "\t\tRestExceptionMapper          (r.v.d.g.t.r.support)      \n" +
                "\t\tRestFilter1                  (r.v.d.g.t.r.support)      \n" +
                "\n" +
                "\tUse .printJerseyConfig() report to see ALL registered jersey extensions (including dropwizard)");
    }

    @TestGuiceyApp(value = RestStubApp.class, debug = true)
    @Disabled
    public static class Test1 {

        @StubRest(disableDropwizardExceptionMappers = true,
                disableResources = Resource2.class,
                disableJerseyExtensions = RestFilter2.class)
        RestClient rest;

        @Test
        void test() {
        }
    }

    @Override
    protected String clean(String out) {
        return out;
    }
}
