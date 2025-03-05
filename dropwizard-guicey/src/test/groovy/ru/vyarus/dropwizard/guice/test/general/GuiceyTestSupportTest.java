package ru.vyarus.dropwizard.guice.test.general;

import com.google.common.base.Preconditions;
import com.google.inject.Key;
import io.dropwizard.configuration.FileConfigurationSourceProvider;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.core.Configuration;
import org.apache.commons.text.StringSubstitutor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.support.TestConfiguration;
import ru.vyarus.dropwizard.guice.support.feature.DummyService;
import ru.vyarus.dropwizard.guice.test.GuiceyTestSupport;
import ru.vyarus.dropwizard.guice.test.util.RunResult;

import java.util.Arrays;

/**
 * @author Vyacheslav Rusakov
 * @since 22.11.2023
 */
public class GuiceyTestSupportTest {

    @Test
    void testGuiceySupportManualUsage() throws Exception {
        GuiceyTestSupport<TestConfiguration> support = new GuiceyTestSupport<>(AutoScanApplication.class, (String) null);
        support.before();
        Assertions.assertThat(support.getBean(DummyService.class)).isNotNull();
        Assertions.assertThat(support.getBean(Key.get(DummyService.class))).isNotNull();
        support.after();

        Assertions.assertThatThrownBy(() -> support.getBean(DummyService.class))
                .hasMessage("Guice injector not available");
    }

    @Test
    void testRunWithinStartedSupport() throws Exception {
        GuiceyTestSupport<TestConfiguration> support = new GuiceyTestSupport<>(AutoScanApplication.class,
                "src/test/resources/ru/vyarus/dropwizard/guice/config.yml",
                new SubstitutingSourceProvider(new FileConfigurationSourceProvider(), new StringSubstitutor()));

        support.run(injector -> {
            Preconditions.checkNotNull(injector.getInstance(DummyService.class));
            return null;
        });
    }

    @Test
    void testRunWithoutManagedLifecycle() throws Exception {
        GuiceyTestSupport<Configuration> support = new GuiceyTestSupport<>(BuilderRunCoreWithoutManagedTest.App.class, (String) null)
                .disableManagedLifecycle();

        RunResult<Configuration> result = support.run();
        BuilderRunCoreWithoutManagedTest.UnusedManaged managed = result.getBean(BuilderRunCoreWithoutManagedTest.UnusedManaged.class);
        Assertions.assertThat(managed.started).isFalse();
        Assertions.assertThat(managed.stopped).isFalse();

        org.junit.jupiter.api.Assertions.assertEquals(Arrays.asList("lifeCycleStarting", "lifeCycleStarted", "lifeCycleStopping", "lifeCycleStopped"),
                result.<BuilderRunCoreWithoutManagedTest.App>getApplication().events);
    }
}
