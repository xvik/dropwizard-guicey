package ru.vyarus.dropwizard.guice.test.jupiter.setup.stub;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.testkit.engine.EngineTestKit;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.stub.StubBean;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * @author Vyacheslav Rusakov
 * @since 11.02.2025
 */
public class FieldValueChangeDetectionTest {

    String msg;

    @Test
    void checkFieldOverrideDetection() {

        EngineTestKit
                .engine("junit-jupiter")
                .configurationParameter("junit.jupiter.conditions.deactivate", "org.junit.*DisabledCondition")
                .selectors(selectClass(Test1.class))
                .execute().allEvents().failed().stream()
                .forEach(event -> {
                    Throwable err = event.getPayload(TestExecutionResult.class).get().getThrowable().get();
                    err.printStackTrace();
                    msg = err.getMessage();
                });

        org.junit.jupiter.api.Assertions.assertEquals("Field FieldValueChangeDetectionTest$Test1.stub1 " +
                "annotated with @StubBean value was changed: most likely, it happen in test setup method, which is " +
                "called after Injector startup and so too late to change binding values. Manual initialization is possible " +
                "in field directly.", msg);
    }

    @Test
    void checkStaticFieldOverrideDetection() {

        EngineTestKit
                .engine("junit-jupiter")
                .configurationParameter("junit.jupiter.conditions.deactivate", "org.junit.*DisabledCondition")
                .selectors(selectClass(Test2.class))
                .execute().allEvents().failed().stream()
                .forEach(event -> {
                    Throwable err = event.getPayload(TestExecutionResult.class).get().getThrowable().get();
                    msg = err.getMessage();
                });

        org.junit.jupiter.api.Assertions.assertEquals("Field FieldValueChangeDetectionTest$Test2.stub2 " +
                "annotated with @StubBean value was changed: most likely, it happen in test setup method, which is called " +
                "after Injector startup and so too late to change binding values. Manual initialization is possible " +
                "in field directly.", msg);
    }


    @TestGuiceyApp(DefaultTestApp.class)
    @Disabled
    public static class Test1 {
        @StubBean(Service1.class)
        Service1Stub stub1;

        @BeforeEach
        void setUp() {
            Assertions.assertThat(stub1).isNotNull();
            stub1 = new Service1Stub();
        }

        @Test
        void testValueOverrideDetection() {
            // error after test
        }

    }

    @TestGuiceyApp(DefaultTestApp.class)
    @Disabled
    public static class Test2 {

        @StubBean(Service2.class)
        static Service2Stub stub2;

        @BeforeAll
        static void beforeAll() {
            Assertions.assertThat(stub2).isNotNull();
            stub2 = new Service2Stub();
        }

        @Test
        void testValueOverrideDetection() {
            // error after test
        }
    }

    public static class Service1Stub extends Service1 {}

    public static class Service2Stub extends Service2 {}

    public static class Service1 {}

    public static class Service2 {}
}
