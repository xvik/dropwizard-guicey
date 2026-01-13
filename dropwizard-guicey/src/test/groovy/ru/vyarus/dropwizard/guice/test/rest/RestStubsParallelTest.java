package ru.vyarus.dropwizard.guice.test.rest;

import io.dropwizard.core.setup.Bootstrap;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.TestSupport;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Vyacheslav Rusakov
 * @since 13.01.2026
 */
public class RestStubsParallelTest {

    ScheduledExecutorService executorService;

    @BeforeEach
    void setUp() {
        executorService = Executors.newScheduledThreadPool(10);
    }

    @AfterEach
    void tearDown() {
        executorService.shutdown();
    }

    @Test
    void testParallelExecution() throws Exception {
        final List<Future<String>> futures = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            futures.add(executorService.submit(() -> {
                try {
                    return TestSupport.build(App.class)
                            .hooks(RestStubsHook.builder().build())
                            .runCore(injector -> {
                                RestClient client = RestClient.lookup(TestSupport.getContext().getApplication());
                                return client.get("/1/", String.class);
                            });
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }));
        }

        // wait
        Set<String> results = new HashSet<>();
        for (Future<String> future : futures) {
            results.add(future.get());
        }

        Assertions.assertThat(results).hasSize(10);
    }

    public static class App extends DefaultTestApp {
        @Override
        protected GuiceBundle configure() {
            return GuiceBundle.builder()
                    .extensions(Resource1.class)
                    .build();
        }
    }

    @Path("/1/")
    @Produces("application/json")
    @Singleton
    public static class Resource1 {

        @Inject
        Bootstrap bootstrap;

        @GET
        @Path("/")
        public String get() {
            // verify different application instances would be used
            return String.valueOf(System.identityHashCode(bootstrap.getApplication()));
        }
    }
}
