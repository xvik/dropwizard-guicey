package ru.vyarus.dropwizard.guice.test.jupiter.setup.track;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.track.TrackBean;
import ru.vyarus.dropwizard.guice.test.track.Tracker;

/**
 * @author Vyacheslav Rusakov
 * @since 14.02.2025
 */
public class TrackerDiagnosticCompatibilityTest extends AbstractTrackerTest {

    @Test
    void checkTrackerWithDiagnosticEnabled() {

        String output = run(Test1.class);

        org.assertj.core.api.Assertions.assertThat(output)
                // traces
                .contains("\\\\\\---[Tracker<GuiceyConfigurationInfo>] 11.11 ms      <@11111111> .getNormalModuleIds() = (1)[ ItemId@11111111 ]")

                // registration debug
                .contains("Applied trackers (@TrackBean) on TrackerDiagnosticCompatibilityTest$Test1:\n" +
                        "\n" +
                        "\t#track                         GuiceyConfigurationInfo      (r.v.d.guice.module)       \n" +
                        "\n")

                // stats report (debug)
                .contains("Trackers stats (sorted by median) for TrackerDiagnosticCompatibilityTest$Test1#testTracker():\n" +
                        "\n" +
                        "\t[service]                                [method]                                           [calls]    [fails]    [min]      [max]      [median]   [75%]      [95%]")

                .contains("GuiceyConfigurationInfo                  getNormalModuleIds()                               1          0          11.11 ms   11.11 ms   11.11 ms   11.11 ms   11.11 ms  ");
    }


    @TestGuiceyApp(value = Test1.App.class, debug = true)
    @Disabled // prevent direct execution
    public static class Test1 {

        @TrackBean(trace = true)
        Tracker<GuiceyConfigurationInfo> track;

        @Test
        void testTracker() {
            Assertions.assertNotNull(track);
        }

        public static class App extends DefaultTestApp {
            @Override
            protected GuiceBundle configure() {
                return GuiceBundle.builder()
                        .printAllGuiceBindings()
                        .build();
            }
        }
    }
}
