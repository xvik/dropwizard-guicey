package ru.vyarus.dropwizard.guice.debug.renderer.guice.support;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matchers;
import ru.vyarus.dropwizard.guice.debug.renderer.guice.support.exts.*;

/**
 * Have to use java class because otherwise guice can't correctly resolve source line, which makes test
 * behave differently ofr different runs.
 *
 * @author Vyacheslav Rusakov
 * @since 20.08.2019
 */
public class CasesModule extends AbstractModule {
    @Override
    protected void configure() {
        bindListener(new AbstractMatcher<TypeLiteral<?>>() {
            @Override
            public boolean matches(TypeLiteral<?> typeLiteral) {
                return false;
            }
        }, new CustomTypeListener());

        bindListener(new AbstractMatcher<Object>() {
            @Override
            public boolean matches(Object o) {
                return false;
            }
        }, new CustomProvisionListener());

        bindInterceptor(Matchers.subclassesOf(AopedService.class), Matchers.any(),
                new CustomAop());

        bind(AopedService.class);
        bind(BindService.class).to(OverriddenService.class);
        bind(BindService2.class).toInstance(new BindService2() {});
    }
}
