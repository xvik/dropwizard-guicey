package ru.vyarus.dropwizard.guice.support.request

import com.google.inject.servlet.RequestScoped

/**
 * @author Vyacheslav Rusakov 
 * @since 04.10.2014
 */
@RequestScoped
class RequestScopedBean {

    static boolean called = false

    String foo() {
        RequestScopedBean.called = true
        return 'foo';
    }
}
