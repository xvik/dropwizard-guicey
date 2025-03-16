package ru.vyarus.dropwizard.guice;

import org.assertj.core.api.Assertions;
import org.eclipse.jetty.util.component.LifeCycle;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Vyacheslav Rusakov
 * @since 14.03.2025
 */

public class ListenersTest extends AbstractPlatformTest{

    @Test
    void testListenerMethods() {
        runSuccess(Test1.class);
        Assertions.assertThat(Test1.actions).isEqualTo(Arrays.asList(
                "whenConfigurationReady",
                "onGuiceyStartup",
                "lifeCycleStarting",
                "onApplicationStartup",
                "lifeCycleStarted",
                "listenServer",
                "lifeCycleStopping",
                "onApplicationShutdown",
                "lifeCycleStopped"
        ));
    }

    @TestDropwizardApp(Test1.App.class)
    @Disabled
    public static class Test1 {
        static List<String> actions = new ArrayList<>();

        @Test
        void test() {
        }

        public static class App extends DefaultTestApp {

            @Override
            protected GuiceBundle configure() {
                return GuiceBundle.builder()
                        .whenConfigurationReady(environment -> actions.add("whenConfigurationReady"))
                        .onGuiceyStartup((config, env, injector) ->
                                actions.add("onGuiceyStartup"))
                        .onApplicationStartup(context -> actions.add("onApplicationStartup"))
                        .onApplicationShutdown(context -> actions.add("onApplicationShutdown"))
                        .listenJetty(new LifeCycle.Listener() {
                            @Override
                            public void lifeCycleStarting(LifeCycle event) {
                                actions.add("lifeCycleStarting");
                            }

                            @Override
                            public void lifeCycleStarted(LifeCycle event) {
                                actions.add("lifeCycleStarted");
                            }

                            @Override
                            public void lifeCycleStopping(LifeCycle event) {
                                actions.add("lifeCycleStopping");
                            }

                            @Override
                            public void lifeCycleStopped(LifeCycle event) {
                                actions.add("lifeCycleStopped");
                            }
                        })
                        .listenServer(server -> actions.add("listenServer"))
                        .build();
            }
        }
    }


    @Override
    protected String clean(String out) {
        return out;
    }
}
