package ru.vyarus.dropwizard.guice.debug;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.ConfiguredBundle;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.AbstractPlatformTest;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.module.context.SharedConfigurationState;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyEnvironment;
import ru.vyarus.dropwizard.guice.module.support.DropwizardAwareModule;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

/**
 * @author Vyacheslav Rusakov
 * @since 21.03.2025
 */
public class SharedStateDiagnosticTest extends AbstractPlatformTest {

    @TestGuiceyApp(App.class)
    @Disabled
    public static class Test1 {
        @Test
        void test() {

        }
    }

    public static class App extends DefaultTestApp {

        @Override
        public void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(new Bundle())
                    .modules(new Mod())
                    .printSharedStateUsage()
                    .withSharedState(state -> {
                        state.get(List.class); // miss
                        state.whenReady(List.class, list -> {
                            // delayed access
                        });
                        state.get(List.class, () -> Arrays.asList("foo", "bar")); // set and get
                        state.getOrFail(List.class, "err");
                        state.whenReady(List.class, list -> {
                            // immediate access
                        });
                        state.get(GuiceBundle.class); // never set
                        state.whenReady(GuiceBundle.class, guiceBundle -> {}); // never set

                        state.whenReady(ConfiguredBundle.class, guiceBundle -> {}); // never set (listener only)
                    })
                    .build());

            // direct access
            SharedConfigurationState.lookupOrFail(this, List.class, "err");
            SharedConfigurationState.getStartupInstance().whenReady(List.class, list -> {
                // immediate access
            });
        }

        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {
            // direct access
            SharedConfigurationState.lookupOrFail(environment, List.class, "err");
            // getStartupInstance() not available here (after guice bundle run)
            SharedConfigurationState.get(environment).get().whenReady(List.class, list -> {
                // immediate access
            });
        }
    }

    public static class Bundle implements GuiceyBundle {
        @Override
        public void initialize(GuiceyBootstrap bootstrap) {
            bootstrap.sharedState(Map.class); // miss
            bootstrap.whenSharedStateReady(Map.class, state -> {
                // delayed
            });
            bootstrap.sharedState(Map.class, HashMap::new); // miss
            bootstrap.whenSharedStateReady(Map.class, state -> {
                // immediate access
            });
            bootstrap.sharedStateOrFail(Map.class, "err");
            bootstrap.shareState(Queue.class, new ArrayDeque()); // set + get

            bootstrap.sharedState(GuiceBundle.class); // never set
            bootstrap.whenSharedStateReady(GuiceBundle.class, guiceBundle -> {}); // never set
        }

        @Override
        public void run(GuiceyEnvironment environment) throws Exception {
            environment.sharedState(Set.class); // miss
            environment.whenSharedStateReady(Set.class, state -> {
                // delayed
            });
            environment.sharedState(Set.class, HashSet::new); // miss
            environment.whenSharedStateReady(Set.class, state -> {
                // immediate access
            });
            environment.sharedStateOrFail(Set.class, "err");
            environment.shareState(Stack.class, new Stack()); // set + get

            environment.sharedState(GuiceBundle.class); // never set
            environment.whenSharedStateReady(GuiceBundle.class, guiceBundle -> {}); // never set
        }
    }

    public static class Mod extends DropwizardAwareModule<Configuration> {
        @Override
        protected void configure() {
            sharedState(Module.class); // miss
            whenSharedStateReady(Module.class, state -> {
                // delayed
            });
            sharedState(Module.class, () -> new AbstractModule() {}); // miss
            whenSharedStateReady(Module.class, state -> {
                // immediate access
            });
            sharedStateOrFail(Module.class, "err");
            shareState(AbstractModule.class, new AbstractModule() {}); // set + get

            sharedState(GuiceBundle.class); // never set
            whenSharedStateReady(GuiceBundle.class, guiceBundle -> {}); // never set
        }
    }

    @Test
    void testStateAccessTracking() {
        String out = runSuccess(Test1.class);

        Assertions.assertThat(out).contains("Shared configuration state usage: \n" +
                "\n" +
                "\tSET Options (ru.vyarus.dropwizard.guice.module.context.option)                      \t at r.v.d.g.m.context.(ConfigurationContext.java:167)\n" +
                "\n" +
                "\tSET List (java.util)                                                                \t at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:60)\n" +
                "\t\tMISS at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:56)\n" +
                "\t\tGET at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:57)\n" +
                "\t\tGET  at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:60)\n" +
                "\t\tGET  at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:61)\n" +
                "\t\tGET  at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:62)\n" +
                "\t\tGET  at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:73)\n" +
                "\t\tGET  at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:74)\n" +
                "\t\tGET  at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:82)\n" +
                "\t\tGET  at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:84)\n" +
                "\n" +
                "\tSET Bootstrap (io.dropwizard.core.setup)                                            \t at r.v.d.g.m.context.(ConfigurationContext.java:806)\n" +
                "\n" +
                "\tSET Map (java.util)                                                                 \t at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:97)\n" +
                "\t\tMISS at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:93)\n" +
                "\t\tGET at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:94)\n" +
                "\t\tGET  at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:97)\n" +
                "\t\tGET  at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:98)\n" +
                "\t\tGET  at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:101)\n" +
                "\n" +
                "\tSET Queue (java.util)                                                               \t at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:102)\n" +
                "\tSET Configuration (io.dropwizard.core)                                              \t at r.v.d.g.m.context.(ConfigurationContext.java:833)\n" +
                "\tSET ConfigurationTree (ru.vyarus.dropwizard.guice.module.yaml)                      \t at r.v.d.g.m.context.(ConfigurationContext.java:834)\n" +
                "\tSET Environment (io.dropwizard.core.setup)                                          \t at r.v.d.g.m.context.(ConfigurationContext.java:835)\n" +
                "\n" +
                "\tSET Set (java.util)                                                                 \t at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:114)\n" +
                "\t\tMISS at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:110)\n" +
                "\t\tGET at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:111)\n" +
                "\t\tGET  at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:114)\n" +
                "\t\tGET  at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:115)\n" +
                "\t\tGET  at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:118)\n" +
                "\n" +
                "\tSET Stack (java.util)                                                               \t at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:119)\n" +
                "\n" +
                "\tSET Module (com.google.inject)                                                      \t at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:133)\n" +
                "\t\tMISS at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:129)\n" +
                "\t\tGET at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:130)\n" +
                "\t\tGET  at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:133)\n" +
                "\t\tGET  at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:134)\n" +
                "\t\tGET  at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:137)\n" +
                "\n" +
                "\tSET AbstractModule (com.google.inject)                                              \t at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:138)\n" +
                "\tSET Injector (com.google.inject)                                                    \t at r.v.d.g.i.lookup.(InjectorLookup.java:72)\n" +
                "\n" +
                "\tNEVER SET GuiceBundle (ru.vyarus.dropwizard.guice)\n" +
                "\t\tMISS at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:65)\n" +
                "\t\tMISS at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:104)\n" +
                "\t\tMISS at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:121)\n" +
                "\t\tMISS at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:140)\n" +
                "\t\tMISS at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:66)\n" +
                "\t\tMISS at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:105)\n" +
                "\t\tMISS at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:122)\n" +
                "\t\tMISS at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:141)\n" +
                "\n" +
                "\tNEVER SET ConfiguredBundle (io.dropwizard.core)\n" +
                "\t\tMISS at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:68)");
    }

    @Override
    protected String clean(String out) {
        return out;
    }
}
