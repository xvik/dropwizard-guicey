package ru.vyarus.dropwizard.guice.test.general;

import com.google.inject.Key;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.support.TestConfiguration;
import ru.vyarus.dropwizard.guice.support.feature.DummyService;
import ru.vyarus.dropwizard.guice.test.GuiceyTestSupport;

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
                .hasMessage("Injector not available");
    }
}
