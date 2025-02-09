package ru.vyarus.dropwizard.guice.test.jupiter.setup.stub;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.testkit.engine.EngineTestKit;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.stub.StubBean;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.stream.SystemOut;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * @author Vyacheslav Rusakov
 * @since 09.02.2025
 */
@ExtendWith(SystemStubsExtension.class)
public class IncorrectNestedStubDeclarationTest {

    @SystemStub
    SystemOut out;

    String msg;

    @Test
    void checkStubValidation() {

        EngineTestKit
                .engine("junit-jupiter")
                .configurationParameter("junit.jupiter.conditions.deactivate", "org.junit.*DisabledCondition")
                .selectors(selectClass(Test1.class))
                .execute().allEvents().failed().stream()
                .forEach(event -> {
                    Throwable err = event.getPayload(TestExecutionResult.class).get().getThrowable().get();
                    msg = err.getMessage();
                });
        System.err.println(out.getText());

        Assertions.assertEquals("Incorrect @StubBean 'r.v.d.g.t.j.s.s.IncorrectNestedStubDeclarationTest$Test1$Inner.stub2' " +
                "declaration: nested test runs under already started application and so new stubs could not be added. " +
                "Either remove stubs in nested tests or run application for each test method (with non-static @RegisterExtension field)", msg);
    }

    @TestGuiceyApp(value = Test1.App.class, debug = true)
    @Disabled // prevent direct execution
    public static class Test1 {

        @StubBean(Service.class)
        ServiceStub stub;

        @Test
        void testStub() {
            Assertions.assertNotNull(stub);
        }

        @Nested
        class Inner {

            @Inject
            Service service;

            @Inject
            Service2 service2;

            @StubBean(Service2.class)
            Service2Stub stub2;

            @Test
            void testStubUsed() {
                Assertions.assertNotNull(stub2);
                Assertions.assertEquals(service, stub);
                Assertions.assertEquals(service2, stub2);
            }
        }

        public static class Service {}
        public static class ServiceStub extends Service {}

        public static class Service2 {}
        public static class Service2Stub extends Service2 {}

        public static class App extends Application<Configuration> {

            @Override
            public void initialize(Bootstrap<Configuration> bootstrap) {
                bootstrap.addBundle(GuiceBundle.builder().build());
            }

            @Override
            public void run(Configuration configuration, Environment environment) throws Exception {
            }
        }
    }
}
