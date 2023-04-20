package ru.vyarus.dropwizard.guice.test.jupiter.dw;

import io.dropwizard.Application;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Vyacheslav Rusakov
 * @since 04.05.2020
 */
public class AnnotatedBaseDwTest extends AnnotatedBaseDw {

    @Test
    void checkExtensionApplied(Application app) {
        Assertions.assertNotNull(app);
    }
}
