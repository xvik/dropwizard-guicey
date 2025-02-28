package ru.vyarus.dropwizard.guice.test.jupiter.setup.log.support;

import com.google.inject.Binder;
import com.google.inject.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vyacheslav Rusakov
 * @since 28.02.2025
 */
public class GModule implements Module {
    private final Logger logger = LoggerFactory.getLogger(GModule.class);

    @Override
    public void configure(Binder binder) {
        logger.trace("Module configured");
    }
}
