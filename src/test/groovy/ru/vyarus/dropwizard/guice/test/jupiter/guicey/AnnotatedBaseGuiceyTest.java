package ru.vyarus.dropwizard.guice.test.jupiter.guicey;

import io.dropwizard.core.Application;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Vyacheslav Rusakov
 * @since 04.05.2020
 */
public class AnnotatedBaseGuiceyTest extends AnnotatedBaseGuicey {

    @Test
    void checkExtensionApplied(Application app) {
        Assertions.assertNotNull(app);
    }
}