package ru.vyarus.dropwizard.guice.debug.renderer.guice.support.exts;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * @author Vyacheslav Rusakov
 * @since 14.09.2019
 */
public class CustomAop implements MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        return invocation.proceed();
    }
}
