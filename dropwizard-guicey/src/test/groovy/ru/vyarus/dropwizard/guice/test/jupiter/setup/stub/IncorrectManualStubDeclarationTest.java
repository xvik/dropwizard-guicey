package ru.vyarus.dropwizard.guice.test.jupiter.setup.stub;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.testkit.engine.EngineTestKit;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
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
public class IncorrectManualStubDeclarationTest {

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

        Assertions.assertEquals("Incorrect @StubBean 'r.v.d.g.t.j.s.s.IncorrectManualStubDeclarationTest$Test1.stub' " +
                "declaration: field value can't be used because guice context starts in beforeAll phase. Either make " +
                "field static or remove value (guice will create instance with guice injector)", msg);
    }


    @TestGuiceyApp(value = DefaultTestApp.class, debug = true)
    @Disabled // prevent direct execution
    public static class Test1 {

        @StubBean(Test1.Service.class)
        Test1.ServiceStub stub = new Test1.ServiceStub();

        @Test
        void test() {
            Assertions.assertNotNull(stub);
        }

        public static class Service {}

        public static class ServiceStub extends Service{}
    }
}
