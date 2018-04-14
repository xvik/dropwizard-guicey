package ru.vyarus.dropwizard.guice.test.configurator.unit;

import com.google.inject.Binder;
import com.google.inject.Module;
import ru.vyarus.dropwizard.guice.test.GuiceyConfiguratorRule;

/**
 * @author Vyacheslav Rusakov
 * @since 13.04.2018
 */
public abstract class BaseTest {

    static GuiceyConfiguratorRule BASE_CONF = new GuiceyConfiguratorRule(
            (builder) -> builder.modules(new XMod()));


    public static class XMod implements Module {
        @Override
        public void configure(Binder binder) {
        }
    }
}
