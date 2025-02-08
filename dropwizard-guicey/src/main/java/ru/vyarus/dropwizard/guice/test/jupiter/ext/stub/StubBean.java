package ru.vyarus.dropwizard.guice.test.jupiter.ext.stub;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Replace any guice service with its stub in test: for example, suppose we have some {@code Service} and
 * we need to modify it for tests, then we extend it with {@code class ServiceStub extends Service} and
 * override required methods. Register stub in test field as {@code @StubBean(Service.class) ServiceStub stub;}
 * (could be a static filed). Internally, overriding guice binding would be created:
 * {@code bind(Service.class).to(ServiceStub.class).in(Singleton.class)} so guice would create stub instead of
 * the original service. Guice would create stub instance, so injections would work inside it.
 * <p>
 * More canonical example with interface: suppose we have {@code bind(IServie.clas).to(ServiceImpl.class))}. In this
 * case, stub could simply implement interface, instead of extending class:
 * {@code class ServiceStub implements IService}. Stub field must declare interface as a binding key:
 * {@code @StubBean(IService.class) ServiceStub stub};
 * <p>
 * Guicey test extension debug option would also activate printing all detected stub fields.
 * <p>
 * Stub object would not be re-created for each test in case of per-class test (where application created once for
 * all test methods). If you need to perform some cleanups between tests, stub class must implement
 * {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.stub.StubLifecycle} and it's before() and after() methods
 * would be called before and after each test method.
 * <p>
 * Just in case: guice injection will also return stabbed bean (because stun instance is created by guice).
 *
 * @author Vyacheslav Rusakov
 * @since 06.02.2025
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface StubBean {

    /**
     * The class used to bind overriding service in all other services (could be service itself or base interface).
     *
     * @return replaced service class
     */
    Class<?> value();
}
