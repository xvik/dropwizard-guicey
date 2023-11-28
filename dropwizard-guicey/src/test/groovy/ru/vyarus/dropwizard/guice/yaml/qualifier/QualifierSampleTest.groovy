package ru.vyarus.dropwizard.guice.yaml.qualifier

import com.google.inject.BindingAnnotation
import com.google.inject.Inject
import com.google.inject.name.Named
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import io.dropwizard.metrics.MetricsFactory
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import spock.lang.Specification

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * @author Vyacheslav Rusakov
 * @since 22.11.2023
 */
// see also QualifiedSampleNullValuesTest
@TestGuiceyApp(value = App, configOverride = ["prop1:1", "ee:11", "ee2:12", "obj1.prop2:2", "obj1.prop3:3"])
class QualifierSampleTest extends Specification {

    @Inject @Named("custom") String prop1
    @Inject @CustomQualifier SubObj obj1
    @Inject @Named("sub-prop") Set<String> prop23
    @Inject @Named("metrics") MetricsFactory metricsFactyry

    // both annotations work
    @Inject @jakarta.inject.Named("ee") String ee
    @Inject @javax.inject.Named("ee2") String ee2
    @Inject @Named("ee") String ee3

    def "Check qualified bindings"() {

        expect:
        prop1 == "1"
        obj1 != null
        obj1.prop2 == "2"
        ee != null
        ee2 != null
        ee3 != null
        prop23 == ["2", "3"] as Set
        metricsFactyry != null
    }

    static class App extends Application<MyConfig> {

        @Override
        void initialize(Bootstrap<MyConfig> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .printCustomConfigurationBindings()
                    .build())
        }

        @Override
        void run(MyConfig configuration, Environment environment) throws Exception {

        }
    }

    static class MyConfig extends Configuration {
        @Named("custom")
        private String prop1
        @CustomQualifier
        private SubObj obj1 = new SubObj()
        @jakarta.inject.Named("ee")
        private String ee
        @javax.inject.Named("ee2")
        private String ee2

        String getProp1() {
            return prop1
        }

        SubObj getObj1() {
            return obj1
        }

        String getEe() {
            return ee
        }

        String getEe2() {
            return ee2
        }

        @Named("metrics")  // dropwizard object bind
        @Override
        MetricsFactory getMetricsFactory() {
            return super.getMetricsFactory()
        }
    }

    static class SubObj {
        private String prop2
        private String prop3

        // aggregated binding (same type + qualifier)
        @Named("sub-prop")
        String getProp2() {
            return prop2
        }

        @Named("sub-prop")
        String getProp3() {
            return prop3
        }
    }
}

@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD])
@BindingAnnotation
public @interface CustomQualifier {}
