package ru.vyarus.dropwizard.guice.test.track;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.TestSupport;

import java.time.temporal.ChronoUnit;

/**
 * @author Vyacheslav Rusakov
 * @since 04.05.2025
 */
public class SlowMethodTest {

    @Test
    void testSlowMethods() throws Exception {
        TrackersHook hook = new TrackersHook();
        hook.track(Service.class)
                .slowMethods(1, ChronoUnit.MILLIS)
                .add();

        final String out = TestSupport.captureOutput(() ->
                TestSupport.build(DefaultTestApp.class)
                        .hooks(hook)
                        .runCore(injector -> {
                            injector.getInstance(Service.class).foo();
                            return null;
                        }));

        Assertions.assertThat(out.replace("\r", "")
                        .replaceAll("@[\\da-z]{6,10}", "@11111111")
                        .replaceAll("\\[\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2},\\d+]", "[2025-22-22 11:11:11]")
                        .replaceAll("\\d+(\\.\\d+)? ms {4,}", "11.11 ms      ")
                        .replaceAll("\\d+(\\.\\d+)? ms", "11.11 ms"))
                .contains("WARN  [2025-22-22 11:11:11] ru.vyarus.dropwizard.guice.test.track.Tracker: \n" +
                "\\\\\\---[Tracker<Service>] 11.11 ms      <@11111111> .foo() = \"foo\"");
    }

    @Test
    void testSlowMethodDisable() throws Exception {
        TrackersHook hook = new TrackersHook();
        hook.track(Service.class)
                // change default
                .slowMethods(1, ChronoUnit.MILLIS)
                .disableSlowMethodsLogging()
                .add();

        final String out = TestSupport.captureOutput(() ->
                TestSupport.build(DefaultTestApp.class)
                        .hooks(hook)
                        .runCore(injector -> {
                            injector.getInstance(Service.class).foo();
                            return null;
                        }));

        Assertions.assertThat(out.replace("\r", "")).doesNotContain("\\\\\\---[Tracker<Service>]");
    }


    public static class Service {
        public String foo() {
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "foo";
        }
    }
}
