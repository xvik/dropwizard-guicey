package ru.vyarus.dropwizard.guice.test.jupiter.dw;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.inject.Injector;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.support.TestConfiguration;
import ru.vyarus.dropwizard.guice.support.feature.DummyService;
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp;
import ru.vyarus.dropwizard.guice.test.jupiter.param.AppAdminPort;
import ru.vyarus.dropwizard.guice.test.jupiter.param.AppPort;
import ru.vyarus.dropwizard.guice.test.jupiter.param.ClientSupport;
import ru.vyarus.dropwizard.guice.test.jupiter.param.Jit;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Vyacheslav Rusakov
 * @since 01.05.2020
 */
@TestDropwizardApp(AutoScanApplication.class)
public class ParametersInjectionDwTest {

    public ParametersInjectionDwTest(Environment env, DummyService service) {
        Preconditions.checkNotNull(env);
        Preconditions.checkNotNull(service);
    }

    @BeforeAll
    static void before(Application app, DummyService service) {
        Preconditions.checkNotNull(app);
        Preconditions.checkNotNull(service);
    }

    @BeforeEach
    void setUp(Application app, DummyService service) {
        Preconditions.checkNotNull(app);
        Preconditions.checkNotNull(service);
    }

    @AfterEach
    void tearDown(Application app, DummyService service) {
        Preconditions.checkNotNull(app);
        Preconditions.checkNotNull(service);
    }

    @AfterAll
    static void after(Application app, DummyService service) {
        Preconditions.checkNotNull(app);
        Preconditions.checkNotNull(service);
    }

    @Test
    void checkAllPossibleParams(Application app,
                                AutoScanApplication app2,
                                Configuration conf,
                                TestConfiguration conf2,
                                Environment env,
                                ObjectMapper mapper,
                                Injector injector,
                                ClientSupport clientSupport,
                                DummyService service,
                                @Jit JitService jit,
                                @AppPort int port,
                                @AppAdminPort int adminPort) {
        assertNotNull(app);
        assertNotNull(app2);
        assertNotNull(conf);
        assertNotNull(conf2);
        assertNotNull(env);
        assertNotNull(mapper);
        assertNotNull(injector);
        assertNotNull(clientSupport);
        assertNotNull(service);
        assertNotNull(jit);
        assertEquals(port, 8080);
        assertEquals(adminPort, 8081);
    }

    public static class JitService {

        private final DummyService service;

        @Inject
        public JitService(DummyService service) {
            this.service = service;
        }
    }
}
