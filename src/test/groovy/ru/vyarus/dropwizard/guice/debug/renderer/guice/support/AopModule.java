package ru.vyarus.dropwizard.guice.debug.renderer.guice.support;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.annotation.Retention;
import java.util.List;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Vyacheslav Rusakov
 * @since 23.08.2019
 */
public class AopModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Service.class);
        bind(Service2.class);
        bindInterceptor(Matchers.inSubpackage("ru.vyarus"), Matchers.any(), new Interceptor1());
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(KK.class), new Interceptor2());
    }

    public static class Service {

        public void something() {}

        public List somethingElse(List other) {
            return null;
        }
    }

    @Retention(RUNTIME)
    public @interface KK {}

    public static class Service2 {

        public void something() {}

        @KK
        public List somethingElse(List other) {
            return null;
        }
    }


    public static class Interceptor1 implements MethodInterceptor {
        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            return invocation.proceed();
        }
    }

    public static class Interceptor2 implements MethodInterceptor {
        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            return invocation.proceed();
        }
    }
}
