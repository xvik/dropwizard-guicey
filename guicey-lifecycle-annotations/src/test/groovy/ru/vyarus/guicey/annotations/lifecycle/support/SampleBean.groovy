package ru.vyarus.guicey.annotations.lifecycle.support

import jakarta.annotation.PostConstruct
import jakarta.inject.Singleton

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
