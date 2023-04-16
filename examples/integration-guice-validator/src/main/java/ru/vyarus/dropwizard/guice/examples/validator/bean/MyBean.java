package ru.vyarus.dropwizard.guice.examples.validator.bean;

/**
 * Example of custom bean with attached validator.
 * Validator will be activated by {@link jakarta.validation.Valid} annotation.
 *
 * @author Vyacheslav Rusakov
 * @since 12.01.2018
 */
@MyBeanValid
public class MyBean {

    private String foo;
    private String bar;

    public String getFoo() {
        return foo;
    }

    public void setFoo(String foo) {
        this.foo = foo;
    }

    public String getBar() {
        return bar;
    }

    public void setBar(String bar) {
        this.bar = bar;
    }
}
