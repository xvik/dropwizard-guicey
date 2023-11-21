package ru.vyarus.dropwizard.guice.test.jupiter;

import com.google.common.base.Preconditions;
import io.dropwizard.testing.DropwizardTestSupport;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.support.TestConfiguration;
import ru.vyarus.dropwizard.guice.test.ClientSupport;
import ru.vyarus.dropwizard.guice.test.TestSupport;
import ru.vyarus.dropwizard.guice.test.builder.TestSupportHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Vyacheslav Rusakov
 * @since 16.11.2023
 */
@TestGuiceyApp(AutoScanApplication.class)
public class StaticContextAccessTest {

    // generified support injection also works
    public StaticContextAccessTest(DropwizardTestSupport<TestConfiguration> support, ClientSupport client) {
        Preconditions.checkNotNull(support);
        Preconditions.checkState(TestSupportHolder.isContextSet());
        Preconditions.checkState(support == TestSupport.<TestConfiguration>getContext());
        Preconditions.checkState(client == TestSupport.getContextClient());
    }

    @BeforeAll
    static void before(DropwizardTestSupport support) {
        Preconditions.checkNotNull(support);
        Preconditions.checkState(TestSupportHolder.isContextSet());
        Preconditions.checkState(support == TestSupportHolder.getContext());
    }

    @BeforeEach
    void setUp(DropwizardTestSupport support) {
        Preconditions.checkNotNull(support);
        Preconditions.checkState(TestSupportHolder.isContextSet());
        Preconditions.checkState(support == TestSupportHolder.getContext());
    }

    @AfterEach
    void tearDown(DropwizardTestSupport support) {
        Preconditions.checkNotNull(support);
        Preconditions.checkState(TestSupportHolder.isContextSet());
        Preconditions.checkState(support == TestSupportHolder.getContext());
    }

    @AfterAll
    static void after(DropwizardTestSupport support) {
        Preconditions.checkNotNull(support);
        Preconditions.checkState(TestSupportHolder.isContextSet());
        Preconditions.checkState(support == TestSupportHolder.getContext());
    }

    @Test
    void test1(DropwizardTestSupport support,
                                ClientSupport client) {
        assertNotNull(client);
        assertNotNull(support);
        assertEquals(support, TestSupport.getContext());
        assertEquals(client, TestSupport.getContextClient());
    }

    @Test
    void test2(DropwizardTestSupport support,
               ClientSupport client) {
        assertNotNull(client);
        assertNotNull(support);
        assertEquals(support, TestSupport.getContext());
        assertEquals(client, TestSupport.getContextClient());
    }

    @Nested
    class Inner {

        @Test
        void test1(DropwizardTestSupport support, ClientSupport client) {
            Assertions.assertEquals(support, TestSupport.getContext());
            Assertions.assertEquals(client, TestSupport.getContextClient());
        }

        @Test
        void test2(DropwizardTestSupport support, ClientSupport client) {
            Assertions.assertEquals(support, TestSupport.getContext());
            Assertions.assertEquals(client, TestSupport.getContextClient());
        }
    }
}
