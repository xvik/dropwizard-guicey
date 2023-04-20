package ru.vyarus.guicey.validation

import ru.vyarus.guicey.validation.util.RestMethodMatcher
import spock.lang.Specification

import javax.validation.executable.ValidateOnExecution
import javax.ws.rs.GET
import javax.ws.rs.POST

/**
 * @author Vyacheslav Rusakov
 * @since 30.12.2019
 */
class RestMethodMatcherTest extends Specification {

    def "Check matcher"() {

        def matcher = new RestMethodMatcher()
        expect: "correct methods recognition"
        matcher.matches(Service.getMethod(name)) == res

        where:
        name              | res
        'method'          | false
        'getMethod'       | true
        'postMethod'      | true
        'otherAnnotation' | false
    }

    static class Service {

        public void method() {}

        @GET
        public void getMethod() {}

        @POST
        public void postMethod() {}

        @ValidateOnExecution
        public void otherAnnotation() {}
    }
}
