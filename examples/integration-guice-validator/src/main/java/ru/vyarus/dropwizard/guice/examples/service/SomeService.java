package ru.vyarus.dropwizard.guice.examples.service;

import ru.vyarus.dropwizard.guice.examples.validator.CustomCondition;
import ru.vyarus.dropwizard.guice.examples.validator.bean.MyBean;

import javax.inject.Singleton;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Sample service to show validation usage.
 *
 * @author Vyacheslav Rusakov
 * @since 12.01.2018
 */
@Singleton
public class SomeService {

    // use existing javax.validation annotation
    public String simpleValidation(@NotNull String value) {
        return value;
    }

    // use custom validator for parameter
    public String customValidationParameter(@CustomCondition String value) {
        return value;
    }

    // use custom validator for return value
    @CustomCondition
    public String customValidationReturn(String value) {
        return value;
    }


    // @Valid trigger @MyBeanValid declared on bean
    public void customBeanCheck(@Valid MyBean bean) {
    }

    public String getSomething() {
        return "foo";
    }
}
