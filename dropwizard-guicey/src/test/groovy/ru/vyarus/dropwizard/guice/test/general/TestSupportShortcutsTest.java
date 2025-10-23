package ru.vyarus.dropwizard.guice.test.general;

import com.google.common.base.Preconditions;
import io.dropwizard.testing.DropwizardTestSupport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.support.TestConfiguration;
import ru.vyarus.dropwizard.guice.support.feature.DummyService;
import ru.vyarus.dropwizard.guice.test.GuiceyTestSupport;
import ru.vyarus.dropwizard.guice.test.TestSupport;
import ru.vyarus.dropwizard.guice.test.util.RunResult;

/**
 * @author Vyacheslav Rusakov
 * @since 17.11.2023
 */
public class TestSupportShortcutsTest {

    @Test
    void testSupportConstruction() throws Exception {
        final GuiceyTestSupport<TestConfiguration> support = TestSupport
                .coreApp(AutoScanApplication.class, null, "foo: 11");
        TestSupport.run(support);
        Assertions.assertEquals(11, support.getConfiguration().foo);

        DropwizardTestSupport<TestConfiguration> support2 = TestSupport
                .webApp(AutoScanApplication.class, null, "foo: 11");
        TestSupport.run(support2);
        Assertions.assertEquals(11, support2.getConfiguration().foo);
    }

    @Test
    void testCoreRun() throws Exception {
        RunResult<TestConfiguration> res = TestSupport.runCoreApp(AutoScanApplication.class);
        Assertions.assertNotNull(res.getSupport());
        Assertions.assertNotNull(res.getConfiguration());
        Assertions.assertNotNull(res.getApplication());
        Assertions.assertNotNull(res.getEnvironment());
        Assertions.assertNotNull(res.getInjector());
        Assertions.assertNotNull(res.getBean(DummyService.class));
        Assertions.assertFalse(res.isWebRun());


        DropwizardTestSupport<TestConfiguration> support = TestSupport.runCoreApp(AutoScanApplication.class,
                injector -> {
                    Preconditions.checkNotNull(injector);
                    Preconditions.checkNotNull(TestSupport.getContextClient());
                    return Preconditions.checkNotNull(TestSupport.getContext());
                });
        Assertions.assertNotNull(support.getConfiguration());


        res = TestSupport.runCoreApp(AutoScanApplication.class,
                "src/test/resources/ru/vyarus/dropwizard/guice/config.yml", "foo: 2", "bar: 12");
        Assertions.assertEquals(2, res.getConfiguration().foo);


        support = TestSupport.runCoreApp(AutoScanApplication.class,
                "src/test/resources/ru/vyarus/dropwizard/guice/config.yml",
                injector -> {
                    Preconditions.checkNotNull(injector);
                    Preconditions.checkNotNull(TestSupport.getContextClient());
                    return Preconditions.checkNotNull(TestSupport.getContext());
                }, "foo: 2", "bar: 12");
        Assertions.assertEquals(2, support.getConfiguration().foo);
    }


    @Test
    void testWebRun() throws Exception {
        RunResult<TestConfiguration> res = TestSupport.runWebApp(AutoScanApplication.class);
        Assertions.assertNotNull(res.getSupport());
        Assertions.assertNotNull(res.getConfiguration());
        Assertions.assertNotNull(res.getApplication());
        Assertions.assertNotNull(res.getEnvironment());
        Assertions.assertNotNull(res.getInjector());
        Assertions.assertNotNull(res.getBean(DummyService.class));
        Assertions.assertTrue(res.isWebRun());


        DropwizardTestSupport<TestConfiguration> support = TestSupport.runWebApp(AutoScanApplication.class,
                injector -> {
                    Preconditions.checkNotNull(injector);
                    Preconditions.checkNotNull(TestSupport.getContextClient());
                    return Preconditions.checkNotNull(TestSupport.getContext());
                });
        Assertions.assertNotNull(support.getConfiguration());


        res = TestSupport.runWebApp(AutoScanApplication.class,
                "src/test/resources/ru/vyarus/dropwizard/guice/config.yml", "foo: 2", "bar: 12");
        Assertions.assertEquals(2, res.getConfiguration().foo);


        support = TestSupport.runWebApp(AutoScanApplication.class,
                "src/test/resources/ru/vyarus/dropwizard/guice/config.yml",
                injector -> {
                    Preconditions.checkNotNull(injector);
                    Preconditions.checkNotNull(TestSupport.getContextClient());
                    return Preconditions.checkNotNull(TestSupport.getContext());
                }, "foo: 2", "bar: 12");
        Assertions.assertEquals(2, support.getConfiguration().foo);


        support = TestSupport.runWebApp(AutoScanApplication.class,
                "src/test/resources/ru/vyarus/dropwizard/guice/config.yml",
                injector -> {
                    Preconditions.checkNotNull(injector);
                    Preconditions.checkNotNull(TestSupport.getContextClient());
                    return Preconditions.checkNotNull(TestSupport.getContext());
                }, "foo: 2", "bar: 12");
        Assertions.assertEquals(2, support.getConfiguration().foo);
    }
}
