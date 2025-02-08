package ru.vyarus.dropwizard.guice.test.jupiter.setup.stub;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.testkit.engine.EngineTestKit;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.stub.StubBean;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * @author Vyacheslav Rusakov
 * @since 08.02.2025
 */
public class IncorrectStubDeclarationTest {

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
                    msg = err.getCause().getMessage();
                });

        Assertions.assertEquals("Incorrect @StubBean 'r.v.d.g.t.j.s.s.IncorrectStubDeclarationTest$Test1.stub' " +
                "declaration: ServiceStub is not assignable to Service", msg);
    }


    @TestGuiceyApp(Test1.App.class)
    @Disabled // prevent direct execution
    public static class Test1 {

        @StubBean(Service.class)
        ServiceStub stub;

        @Test
        void test() {
            Assertions.assertNotNull(stub);
        }

        public static class App extends Application<Configuration> {

            @Override
            public void initialize(Bootstrap<Configuration> bootstrap) {
                bootstrap.addBundle(GuiceBundle.builder().build());
            }

            @Override
            public void run(Configuration configuration, Environment environment) throws Exception {
            }
        }

        public static class Service {}

        public static class ServiceStub {}
    }
}
