package ru.vyarus.dropwizard.guice.test


import ru.vyarus.dropwizard.guice.support.AutoScanApplication
import spock.lang.Specification

import static io.dropwizard.testing.ConfigOverride.config

/**
 * @author Brian Wehrle
 * @since 05.03.2021
 */

class GuiceyRuleConfigOverrideTest extends Specification {

    private final static String CONFIG_PROPERTY = "server.type";
    private final static String CONFIG_PROPERTY_VALUE = "default";
    private final static String PROPERTY_PREFIX = "dw";

    @SuppressWarnings('GrDeprecatedAPIUsage')
    def "Test config override not applied in init"() {
        GuiceyAppRule guiceyAppRule = new GuiceyAppRule<>(AutoScanApplication,
                null,
                config(CONFIG_PROPERTY, CONFIG_PROPERTY_VALUE));

        expect: "config override not applied after init"
        !isPropertyPresent()
    }

    def "Test config override applied in before"() {
        GuiceyAppRule guiceyAppRule = new GuiceyAppRule<>(AutoScanApplication,
                null,
                config(CONFIG_PROPERTY, CONFIG_PROPERTY_VALUE));

        expect: "config override applied after before()"
        guiceyAppRule.before()
        isPropertyPresent()
    }

    def static isPropertyPresent() {
        Object value = System.getProperties().get(PROPERTY_PREFIX + "." + CONFIG_PROPERTY);
        return value.toString().equals(CONFIG_PROPERTY_VALUE);
    }
}