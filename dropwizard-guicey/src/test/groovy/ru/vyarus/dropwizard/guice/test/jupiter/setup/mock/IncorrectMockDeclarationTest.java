package ru.vyarus.dropwizard.guice.test.jupiter.setup.mock;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.mock.MockBean;

/**
 * @author Vyacheslav Rusakov
 * @since 19.02.2025
 */
public class IncorrectMockDeclarationTest extends AbstractMockTest {

    @Test
    void testIncorrectManualMockDeclaration() {
        
        Throwable th = runFailed(Test1.class);

        Assertions.assertThat(th.getMessage()).contains(
                "Incorrect @MockBean 'IncorrectMockDeclarationTest$Test1.serviceMock' declaration: initialized " +
                        "instance is not a mockito mock object. Either provide correct mock or remove value and let " +
                        "extension create mock automatically.");
    }

    @TestGuiceyApp(DefaultTestApp.class)
    @Disabled
    public static class Test1 {

        static Service mock = new Service();

        @MockBean
        static Service serviceMock = mock;

        @Test
        void testManualMock() {
            Assertions.assertThat(serviceMock).isSameAs(mock);
        }

    }

    public static class Service {
        public String foo() {
            return "foo";
        }
    }
}
