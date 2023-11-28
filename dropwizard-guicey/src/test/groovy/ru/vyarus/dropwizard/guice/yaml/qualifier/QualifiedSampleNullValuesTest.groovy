package ru.vyarus.dropwizard.guice.yaml.qualifier

import com.google.inject.Inject
import com.google.inject.name.Named
import io.dropwizard.metrics.common.MetricsFactory
import jakarta.annotation.Nullable
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 28.11.2023
 */
// see QualifierSampleTest
@TestGuiceyApp(QualifierSampleTest.App)
class QualifiedSampleNullValuesTest extends Specification {

    @Inject @Nullable @Named("custom") String prop1
    @Inject @CustomQualifier QualifierSampleTest.SubObj obj1
    @Inject @Named("sub-prop") Set<String> prop23
    @Inject @Named("metrics") MetricsFactory metricsFactyry

    // both annotations work
    @Inject @Nullable @jakarta.inject.Named("ee") String ee
    @Inject @Nullable @Named("ee") String ee2

    def "Check qualified bindings"() {

        expect:
        prop1 == null
        obj1 != null
        obj1.prop2 == null
        ee == null
        ee2 == null
        prop23 == [null] as Set
        metricsFactyry != null
    }


}
