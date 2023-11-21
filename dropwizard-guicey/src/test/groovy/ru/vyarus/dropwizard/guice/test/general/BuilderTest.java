package ru.vyarus.dropwizard.guice.test.general;

import com.google.common.base.Preconditions;
import com.google.inject.Injector;
import io.dropwizard.testing.DropwizardTestSupport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.support.TestConfiguration;
import ru.vyarus.dropwizard.guice.support.client.CustomTestClientFactory;
import ru.vyarus.dropwizard.guice.support.feature.DummyExceptionMapper;
import ru.vyarus.dropwizard.guice.support.feature.DummyManaged;
import ru.vyarus.dropwizard.guice.test.ClientSupport;
import ru.vyarus.dropwizard.guice.test.GuiceyTestSupport;
import ru.vyarus.dropwizard.guice.test.TestSupport;
import ru.vyarus.dropwizard.guice.test.builder.TestSupportBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Vyacheslav Rusakov
 * @since 16.11.2023
 */
public class BuilderTest {

    @Test
    void testCoreAppRun() throws Exception {

        final List<String> tracker = new ArrayList<>();
        TestConfiguration config = build(tracker)
                .runCore(injector -> {
                    Preconditions.checkNotNull(TestSupport.getContext());
                    final ClientSupport client = TestSupport.getContextClient();
                    Preconditions.checkNotNull(client);

                    Preconditions.checkState(CustomTestClientFactory.getCalled() == 0);
                    client.getClient(); // trigger factory usage

                    // hooks applied
                    GuiceyConfigurationInfo info = injector.getInstance(GuiceyConfigurationInfo.class);
                    Preconditions.checkState(info.getExtensionsDisabled().contains(DummyManaged.class));
                    Preconditions.checkState(info.getExtensionsDisabled().contains(DummyExceptionMapper.class));

                    return injector.getInstance(TestConfiguration.class);
                });

        Assertions.assertIterableEquals(Arrays.asList("setup", "run", "stop", "cleanup"), tracker);
        Assertions.assertEquals(1, CustomTestClientFactory.getCalled());
        Assertions.assertEquals(config.foo, 2);
        Assertions.assertEquals(config.bar, 12);
        Assertions.assertEquals(config.baa, 4);
    }

    @Test
    void testCoreAppCreation() throws Exception {
        DropwizardTestSupport<TestConfiguration> support = build(null).buildCore();

        Assertions.assertNotNull(support);
        Assertions.assertTrue(GuiceyTestSupport.class.isAssignableFrom(support.getClass()));
        TestSupport.run(support);

        Assertions.assertEquals(0, CustomTestClientFactory.getCalled());
        Assertions.assertEquals(support.getConfiguration().foo, 2);
        Assertions.assertEquals(support.getConfiguration().bar, 12);
        Assertions.assertEquals(support.getConfiguration().baa, 4);
    }

    @Test
    void testWebAppRun() throws Exception {

        final List<String> tracker = new ArrayList<>();
        TestConfiguration config = build(tracker)
                .runWeb(injector -> {
                    Preconditions.checkNotNull(TestSupport.getContext());
                    final ClientSupport client = TestSupport.getContextClient();
                    Preconditions.checkNotNull(client);

                    Preconditions.checkState(CustomTestClientFactory.getCalled() == 0);
                    client.getClient(); // trigger factory usage

                    // hooks applied
                    GuiceyConfigurationInfo info = injector.getInstance(GuiceyConfigurationInfo.class);
                    Preconditions.checkState(info.getExtensionsDisabled().contains(DummyManaged.class));
                    Preconditions.checkState(info.getExtensionsDisabled().contains(DummyExceptionMapper.class));

                    return injector.getInstance(TestConfiguration.class);
                });

        Assertions.assertIterableEquals(Arrays.asList("setup", "run", "stop", "cleanup"), tracker);
        Assertions.assertEquals(1, CustomTestClientFactory.getCalled());
        Assertions.assertEquals(config.foo, 2);
        Assertions.assertEquals(config.bar, 12);
        Assertions.assertEquals(config.baa, 4);
    }

    @Test
    void testWebAppCreation() throws Exception {
        DropwizardTestSupport<TestConfiguration> support = build(null).buildWeb();

        Assertions.assertNotNull(support);
        Assertions.assertFalse(GuiceyTestSupport.class.isAssignableFrom(support.getClass()));
        TestSupport.run(support);

        Assertions.assertEquals(0, CustomTestClientFactory.getCalled());
        Assertions.assertEquals(support.getConfiguration().foo, 2);
        Assertions.assertEquals(support.getConfiguration().bar, 12);
        Assertions.assertEquals(support.getConfiguration().baa, 4);
    }

    @Test
    void testRunWithoutCallback() throws Exception {
        final List<String> tracker = new ArrayList<>();
        DropwizardTestSupport<TestConfiguration> support = build(tracker).runCore();

        Assertions.assertTrue(GuiceyTestSupport.class.isAssignableFrom(support.getClass()));
        Assertions.assertIterableEquals(Arrays.asList("setup", "run", "stop", "cleanup"), tracker);
        Assertions.assertEquals(0, CustomTestClientFactory.getCalled());
        Assertions.assertEquals(support.getConfiguration().foo, 2);
        Assertions.assertEquals(support.getConfiguration().bar, 12);
        Assertions.assertEquals(support.getConfiguration().baa, 4);


        tracker.clear();
        support = build(tracker).runWeb();

        Assertions.assertFalse(GuiceyTestSupport.class.isAssignableFrom(support.getClass()));
        Assertions.assertIterableEquals(Arrays.asList("setup", "run", "stop", "cleanup"), tracker);
        Assertions.assertEquals(0, CustomTestClientFactory.getCalled());
        Assertions.assertEquals(support.getConfiguration().foo, 2);
        Assertions.assertEquals(support.getConfiguration().bar, 12);
        Assertions.assertEquals(support.getConfiguration().baa, 4);
    }

    @Test
    void testSupportCreationWithListener() {

        IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class,
                () -> build(new ArrayList<>()).buildCore());
        Assertions.assertEquals("Listeners could be used only with run* methods.", ex.getMessage());


        ex = Assertions.assertThrows(IllegalStateException.class,
                () -> build(new ArrayList<>()).buildWeb());
        Assertions.assertEquals("Listeners could be used only with run* methods.", ex.getMessage());
    }

    protected TestSupportBuilder<TestConfiguration> build(List<String> listenerTracker) {
        TestSupportBuilder<TestConfiguration> res = TestSupport.build(AutoScanApplication.class)
                .config("src/test/resources/ru/vyarus/dropwizard/guice/config.yml")
                .configOverrides("foo: 2", "bar: 12")
                .propertyPrefix("custom")
                .randomPorts()
                .restMapping("api")
                .hooks(Hook.class)
                .hooks(builder -> builder.disableExtensions(DummyManaged.class))
                .clientFactory(new CustomTestClientFactory());

        if (listenerTracker != null) {
            res.listen(new TestSupportBuilder.TestListener<>() {
                @Override
                public void setup(DropwizardTestSupport<TestConfiguration> support) throws Exception {
                    Preconditions.checkNotNull(support);
                    listenerTracker.add("setup");
                }

                @Override
                public void run(DropwizardTestSupport<TestConfiguration> support, Injector injector) throws Exception {
                    Preconditions.checkNotNull(support);
                    Preconditions.checkNotNull(injector);
                    listenerTracker.add("run");
                }

                @Override
                public void stop(DropwizardTestSupport<TestConfiguration> support, Injector injector) throws Exception {
                    Preconditions.checkNotNull(support);
                    Preconditions.checkNotNull(injector);
                    listenerTracker.add("stop");
                }

                @Override
                public void cleanup(DropwizardTestSupport<TestConfiguration> support) throws Exception {
                    Preconditions.checkNotNull(support);
                    listenerTracker.add("cleanup");
                }
            });
        }
        return res;
    }

    public static class Hook implements GuiceyConfigurationHook {

        @Override
        public void configure(GuiceBundle.Builder builder) {
            builder.disableExtensions(DummyExceptionMapper.class);
        }
    }
}
