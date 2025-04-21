package ru.vyarus.dropwizard.guice.test.jupiter.setup.rest;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.AbstractPlatformTest;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.test.EnableHook;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.rest.RestClient;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest;
import ru.vyarus.dropwizard.guice.test.rest.support.RestStubApp;

/**
 * @author Vyacheslav Rusakov
 * @since 24.02.2025
 */
public class JerseyReportCompatibilityTest extends AbstractPlatformTest {

    @Test
    void testJerseyReport() {
         String out = run(Test1.class);

         Assertions.assertThat(out).contains("ru.vyarus.dropwizard.guice.debug.JerseyConfigDiagnostic: Jersey configuration = \n" +
                 "\n" +
                 "    Exception mappers\n" +
                 "        Throwable                      ExceptionMapperBinder$1      (i.d.core.setup)           \n" +
                 "        Throwable                      DefaultExceptionMapper       (o.g.jersey.server)        \n" +
                 "        EofException                   EarlyEofExceptionMapper      (i.d.jersey.errors)        \n" +
                 "        EmptyOptionalException         EmptyOptionalExceptionMapper (i.d.jersey.optional)      \n" +
                 "        IllegalStateException          IllegalStateExceptionMapper  (i.d.jersey.errors)        \n" +
                 "        JerseyViolationException       JerseyViolationExceptionMapper (i.d.j.validation)         \n" +
                 "        JsonProcessingException        JsonProcessingExceptionMapper (i.d.jersey.jackson)       \n" +
                 "        Exception                      RestExceptionMapper          (r.v.d.g.t.r.support)      \n" +
                 "        ValidationException            ValidationExceptionMapper    (o.g.j.s.v.internal)");
    }

    @TestGuiceyApp(RestStubApp.class)
    @Disabled
    public static class Test1 {

        @EnableHook
        static GuiceyConfigurationHook hook = GuiceBundle.Builder::printJerseyConfig;

        @StubRest
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
