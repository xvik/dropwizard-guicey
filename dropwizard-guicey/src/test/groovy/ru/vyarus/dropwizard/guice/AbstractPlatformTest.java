package ru.vyarus.dropwizard.guice;

import com.google.common.base.Preconditions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.testkit.engine.EngineTestKit;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.stream.SystemOut;

import java.util.Arrays;

/**
 * @author Vyacheslav Rusakov
 * @since 19.02.2025
 */
@ExtendWith(SystemStubsExtension.class)
public abstract class AbstractPlatformTest {

    @SystemStub
    SystemOut out;

    Throwable th;

    protected Throwable runFailed(Class... tests) {
        run(tests);
        return Preconditions.checkNotNull(th, "Exception expected, but was not thrown");
    }

    protected String runSuccess(Class... tests) {
        String res = run(tests);
        Preconditions.checkState(th == null, "Exception was not expected, but thrown: %s", th != null ? th.getMessage() : null);
        return res;
    }

    protected String run(Class... tests) {
        th = null;
        EngineTestKit
                .engine("junit-jupiter")
                .configurationParameter("junit.jupiter.conditions.deactivate", "org.junit.*DisabledCondition")
                .selectors(Arrays.stream(tests)
                        .map(DiscoverySelectors::selectClass)
                        .toArray(DiscoverySelector[]::new))
                .execute().allEvents().failed().stream()
                .forEach(event -> {
                    Throwable err = event.getPayload(TestExecutionResult.class).get().getThrowable().get();
                    err.printStackTrace();
                    th = err;
                });

        return doClean(out.getText());
    }


    protected String doClean(String out) {
        // use err because out is redirected
        System.err.println(out);

        String res = clean(out.replace("\r", ""));

        if (!res.isEmpty()) {
            System.err.println("Cleared -------------------------------------------------------");
            System.err.println(res);
            System.err.println("---------------------------------------------------------------");
        }
        return res;

    }

    protected abstract String clean(String out);
}
