package ru.vyarus.dropwizard.guice.injector.jersey.web;

import org.glassfish.jersey.process.internal.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vyacheslav Rusakov
 * @since 05.05.2019
 */
public class GuiceRequestContext implements RequestContext {
    private final Logger logger = LoggerFactory.getLogger(GuiceRequestContext.class);

    @Override
    public RequestContext getReference() {
        logger.debug("RC get reference");
        return this;
    }

    @Override
    public void release() {
        logger.debug("RC release");
    }
}
