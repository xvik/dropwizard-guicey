package ru.vyarus.dropwizard.guice.examples

import ru.vyarus.dropwizard.guice.examples.service.SomeService
import ru.vyarus.dropwizard.guice.examples.validator.bean.MyBean
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import spock.lang.Specification

import jakarta.inject.Inject
import jakarta.validation.ConstraintViolationException

/**
 * @author Vyacheslav Rusakov
 * @since 12.01.2018
 */
@TestGuiceyApp(GValApplication)
class ServiceValidationTest extends Specification {

    @Inject
    SomeService service

    static {
        Locale.setDefault(Locale.ENGLISH)
    }

    def "Check method validation success"() {

        when: "simple validation"
        service.simpleValidation("foo")
        then: "ok"
        true

        when: "custom validation on parameter"
        service.customValidationParameter(service.getSomething())
        then: "ok"
        true

        when: "custom validation on return"
        service.customValidationReturn(service.getSomething())
        then: "ok"
        true

        when: "custom bean validation"
        service.customBeanCheck(new MyBean(foo: service.getSomething()))
        then: "ok"
        true
    }

    def "Check method validation fail"() {

        when: "simple validation"
        service.simpleValidation(null)
        then: "err"
        def ex = thrown(ConstraintViolationException)
        ex.constraintViolations.first().message == 'must not be null'

        when: "custom validation on parameter"
        service.customValidationParameter('bee')
        then: "err"
        ex = thrown(ConstraintViolationException)
        ex.constraintViolations.first().message == 'Very specific case check failed'

        when: "custom validation on return"
        service.customValidationReturn('bee')
        then: "err"
        ex = thrown(ConstraintViolationException)
        ex.constraintViolations.first().message == 'Very specific case check failed'

        when: "custom bean validation"
        service.customBeanCheck(new MyBean(foo: 'bee'))
        then: "err"
        ex = thrown(ConstraintViolationException)
        ex.constraintViolations.first().message == 'Bean is not valid'
    }
}