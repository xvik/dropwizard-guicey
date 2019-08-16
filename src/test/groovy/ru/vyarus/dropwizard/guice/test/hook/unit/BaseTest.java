package ru.vyarus.dropwizard.guice.test.hook.unit;

import com.google.inject.Binder;
import com.google.inject.Module;
import ru.vyarus.dropwizard.guice.test.GuiceyHooksRule;

/**
 * @author Vyacheslav Rusakov
 * @since 13.04.2018
 */
public abstract class BaseTest {

    static GuiceyHooksRule BASE_CONF = new GuiceyHooksRule(
            (builder) -> builder.modules(new XMod()));


    public static class XMod implements Module {
        @Override
        public void configure(Binder binder) {
        }
    }
}
