package ru.vyarus.dropwizard.guice.test.jupiter.setup.rest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.AbstractPlatformTest;
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.RestClient;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest;
import ru.vyarus.dropwizard.guice.test.jupiter.setup.rest.support.RestStubApp;

/**
 * @author Vyacheslav Rusakov
 * @since 25.02.2025
 */
public class IncorrectDeclarationTest extends AbstractPlatformTest {

    @Test
    void testUsageWithDwTest() {

        final Throwable ex = runFailed(Test1.class);
        Assertions.assertEquals("Resources stubbing is useless when application is fully started. Use it with " +
                "@TestGuiceyApp where web services not started in order to start lightweight container with rest services.", ex.getMessage());
    }

    @Test
    void testMultipleFields() {

        final Throwable ex = runFailed(Test2.class);
        Assertions.assertEquals("Multiple @StubRest fields declared. To avoid confusion with the configuration, " +
                "only one field is supported.", ex.getMessage());
    }

    @TestDropwizardApp(RestStubApp.class)
    @Disabled
    public static class Test1 {

        @StubRest
        RestClient rest;

        @Test
        void test() {

        }
    }

    @TestDropwizardApp(RestStubApp.class)
    @Disabled
    public static class Test2 {

        @StubRest
        RestClient rest;


        @StubRest
        RestClient rest2;

        @Test
        void test() {

        }
    }


    @Override
    protected String clean(String out) {
        return out;
    }
}
