package ru.vyarus.dropwizard.guice.injector.jersey;

import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.InjectionManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vyacheslav Rusakov
 * @since 23.04.2019
 */
public class GuiceInjectionFactory implements InjectionManagerFactory {
    private final Logger logger = LoggerFactory.getLogger(GuiceInjectionFactory.class);

    @Override
    public InjectionManager create(Object parent) {
        logger.debug("CREATE injection manager");
        return new GuiceInjectionManager();
    }
}
