package ru.vyarus.dropwizard.guice.debug.provision;

import com.google.inject.Injector;
import javax.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.debug.hook.GuiceProvisionTimeHook;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.EnableHook;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;

/**
 * @author Vyacheslav Rusakov
 * @since 25.03.2025
 */
@TestGuiceyApp(DefaultTestApp.class)
public class StandaloneUsageTest {

    @EnableHook
    static GuiceProvisionTimeHook report = new GuiceProvisionTimeHook();

    @Inject
    Injector injector;

    @Test
    void testRuntimeReport() {

        report.clearData();
        injector.getInstance(Service.class);
        injector.getInstance(Service.class);

        Assertions.assertThat(report.getRecordedData().keys().size()).isEqualTo(2);

        String res = report.renderReport();
        System.out.println(res);

        Assertions.assertThat(res).contains(
                "JIT                  [@Prototype]     Service                                                                          x2");
    }

    public static class Service {}
}
