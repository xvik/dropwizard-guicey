package ru.vyarus.dropwizard.guice.support.feature

import io.dropwizard.lifecycle.Managed

/**
 * @author Vyacheslav Rusakov 
 * @since 03.09.2014
 */
class DummyManaged implements Managed {

    @Override
    void start() throws Exception {

    }

    @Override
    void stop() throws Exception {

    }
}
