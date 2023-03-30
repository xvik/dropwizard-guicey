package ru.vyarus.dropwizard.guice.examples.validator;

import ru.vyarus.dropwizard.guice.examples.service.SomeService;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator used for {@link CustomCondition} anotation.
 *
 * @author Vyacheslav Rusakov
 * @since 12.01.2018
 */
public class CustomValidator implements ConstraintValidator<CustomCondition, String> {

    @Inject
    SomeService service;

    @Override
    public void initialize(CustomCondition constraintAnnotation) {
        // annotation without parameters - no need for processing
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // use guice bean for validation
        return service.getSomething().equals(value);
    }
}
