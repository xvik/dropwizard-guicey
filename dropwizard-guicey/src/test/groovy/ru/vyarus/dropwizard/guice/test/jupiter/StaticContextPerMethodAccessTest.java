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
import org.junit.jupiter.api.extension.RegisterExtension;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.test.ClientSupport;
import ru.vyarus.dropwizard.guice.test.TestSupport;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.TestGuiceyAppExtension;
import ru.vyarus.dropwizard.guice.test.builder.TestSupportHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Vyacheslav Rusakov
 * @since 16.11.2023
 */
public class StaticContextPerMethodAccessTest {
    @RegisterExtension
    TestGuiceyAppExtension extension = TestGuiceyAppExtension.forApp(AutoScanApplication.class).create();

    // parameters can't be used in constructor and beforeAll/afterAll due to per-method execution
    public StaticContextPerMethodAccessTest() {
        Preconditions.checkState(!TestSupportHolder.isContextSet());
    }

    @BeforeAll
    static void before() {
        Preconditions.checkState(!TestSupportHolder.isContextSet());
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
    static void after() {
        Preconditions.checkState(!TestSupportHolder.isContextSet());
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
