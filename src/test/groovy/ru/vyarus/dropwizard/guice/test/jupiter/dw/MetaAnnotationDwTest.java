package ru.vyarus.dropwizard.guice.test.jupiter.dw;

import io.dropwizard.core.Application;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Vyacheslav Rusakov
 * @since 04.05.2020
 */
@MetaAnnotationDwTest.MyApp
public class MetaAnnotationDwTest {

    @Test
    void checkAnnotationRecognized(Application app) {
        Assertions.assertNotNull(app);
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @TestDropwizardApp(AutoScanApplication.class)
    public @interface MyApp {
    }
}
