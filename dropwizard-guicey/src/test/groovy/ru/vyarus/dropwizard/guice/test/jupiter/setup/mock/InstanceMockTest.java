package ru.vyarus.dropwizard.guice.test.jupiter.setup.mock;

import com.google.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.mock.MockBean;

/**
 * @author Vyacheslav Rusakov
 * @since 17.02.2025
 */
@TestGuiceyApp(InstanceMockTest.App.class)
public class InstanceMockTest {

    @Inject
    Service service;

    @MockBean
    Service mock;

    @BeforeEach
    void setUp() {
        Mockito.when(mock.foo()).thenReturn("bar");
    }

    @Test
    void testMockedInstance() {
        Assertions.assertThat(service).isEqualTo(mock);
        Assertions.assertThat(service.foo()).isEqualTo("bar");
    }

    public static class App extends DefaultTestApp {
        @Override
        protected GuiceBundle configure() {
            return GuiceBundle.builder()
                    // mocks override existing binding and so instances could be mocked
                    .modules(binder -> binder.bind(Service.class).toInstance(new Service()))
                    .build();
        }
    }

    public static class Service {
        public String foo() {
            return "foo";
        }
    }
}
