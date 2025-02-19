package ru.vyarus.dropwizard.guice.test.jupiter.setup.mock;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.mock.MockBean;

/**
 * @author Vyacheslav Rusakov
 * @since 19.02.2025
 */
@TestGuiceyApp(value = DefaultTestApp.class, debug = true)
public class ManualMockTest {

    static Service mock = Mockito.mock(Service.class);

    @MockBean
    static Service serviceMock = mock;

    @Test
    void testManualMock() {
        Assertions.assertThat(serviceMock).isSameAs(mock);
    }

    public static class Service {
        public String foo() {
            return "foo";
        }
    }
}
