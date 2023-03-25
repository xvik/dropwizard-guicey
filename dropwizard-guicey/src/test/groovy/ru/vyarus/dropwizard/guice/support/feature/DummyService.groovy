package ru.vyarus.dropwizard.guice.support.feature

import ru.vyarus.dropwizard.guice.module.installer.feature.eager.EagerSingleton

/**
 * @author Vyacheslav Rusakov 
 * @since 03.09.2014
 */
@EagerSingleton
class DummyService {

    public String hey() {
        'hey!'
    }
}
