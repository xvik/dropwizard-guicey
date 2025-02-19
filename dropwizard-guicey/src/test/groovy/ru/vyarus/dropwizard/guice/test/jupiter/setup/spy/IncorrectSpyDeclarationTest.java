package ru.vyarus.dropwizard.guice.test.jupiter.setup.spy;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.mock.MockBean;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.spy.SpyBean;
import ru.vyarus.dropwizard.guice.test.jupiter.setup.mock.IncorrectMockDeclarationTest;

/**
 * @author Vyacheslav Rusakov
 * @since 19.02.2025
 */
public class IncorrectSpyDeclarationTest extends AbstractSpyTest {

    @Test
    void testIncorrectManualSpyDeclaration() {

        Throwable th = runFailed(Test1.class);

        Assertions.assertThat(th.getMessage()).contains(
                "Incorrect @SpyBean 'IncorrectSpyDeclarationTest$Test1.serviceMock' declaration: manual spy " +
                        "declaration is not supported. Use @MockBean instead to specify manual spy object.");
    }

    @TestGuiceyApp(DefaultTestApp.class)
    @Disabled
    public static class Test1 {

        static Service mock = new Service();

        @SpyBean
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
