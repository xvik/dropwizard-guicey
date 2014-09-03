package ru.vyarus.dropwizard.guice.module.jersey;


import com.sun.jersey.guice.JerseyServletModule;

/**
 * Guice jersey integration module.
 * Based on source from <a href="https://github.com/HubSpot/dropwizard-guice">dropwizard-guice</a>.
 * @author eliast
 * @author Vyacheslav Rusakov
 * @since 31.08.2014
 */
public class JerseyContainerModule extends JerseyServletModule {

    @Override
    protected void configureServlets() {
        bind(GuiceContainer.class).asEagerSingleton();
    }
}

