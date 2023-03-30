package ru.vyarus.dropwizard.guice.examples.validator.bean;

import ru.vyarus.dropwizard.guice.examples.service.SomeService;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator used to validate {@link MyBean}.
 *
 * @author Vyacheslav Rusakov
 * @since 12.01.2018
 */
public class MyBeanValidator implements ConstraintValidator<MyBeanValid, MyBean> {

    @Inject
    SomeService service;

    @Override
    public void initialize(MyBeanValid constraintAnnotation) {
        // no custom parameters
    }

    @Override
    public boolean isValid(MyBean value, ConstraintValidatorContext context) {
        return service.getSomething().equals(value.getFoo());
    }
}
