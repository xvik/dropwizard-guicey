package ru.vyarus.guicey.annotations.lifecycle.support

import javax.annotation.PostConstruct
import javax.inject.Singleton

/**
 * @author Vyacheslav Rusakov
 * @since 27.11.2018
 */
@Singleton
class SampleBean {

    boolean called

    @PostConstruct
    private void init() {
        called = true
    }
}
