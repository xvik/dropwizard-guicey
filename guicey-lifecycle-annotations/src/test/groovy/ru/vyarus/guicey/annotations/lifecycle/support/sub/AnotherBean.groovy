package ru.vyarus.guicey.annotations.lifecycle.support.sub

import javax.annotation.PostConstruct

/**
 * @author Vyacheslav Rusakov
 * @since 27.11.2018
 */
class AnotherBean {
    boolean called

    @PostConstruct
    private void init() {
        called = true
    }
}
