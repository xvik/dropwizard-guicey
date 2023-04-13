package ru.vyarus.dropwizard.guice.test.jupiter;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.GuiceBundle;

import jakarta.inject.Named;
import jakarta.inject.Provider;

/**
 * @author Vyacheslav Rusakov
 * @since 01.06.2020
 */
@TestGuiceyApp(QualifiedAndParameterizedParamInjectionTest.App.class)
public class QualifiedAndParameterizedParamInjectionTest {

    @Test
    void testQualifiedInjection(@com.google.inject.name.Named("q1") TestBean1 q1,
                                @Named("q2") TestBean1 q2,
                                TestBean2<String> bean2,
                                Provider<TestBean2<String>> bean2Provider) {
        Assertions.assertNotNull(q1);
        Assertions.assertNotNull(q2);
        Assertions.assertNotEquals(q1, q2);

        Assertions.assertNotNull(bean2);
        Assertions.assertNotNull(bean2Provider);
        Assertions.assertEquals(bean2, bean2Provider.get());
    }

    static class TestBean1 {
    }

    static class TestBean2<T> {
    }

    static class Mod extends AbstractModule {
        @Override
        protected void configure() {
            bind(TestBean1.class).annotatedWith(Names.named("q1")).toInstance(new TestBean1());
            bind(new TypeLiteral<TestBean2<String>>() {
            }).toInstance(new TestBean2<>());
        }

        @Provides
        @Named("q2")
        public TestBean1 provide() {
            return new TestBean1();
        }
    }

    public static class App extends Application<Configuration> {

        @Override
        public void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .modules(new Mod())
                    .build());
        }

        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
