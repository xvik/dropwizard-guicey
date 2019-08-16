package ru.vyarus.dropwizard.guice.test.hook.unit;

import com.google.inject.Binder;
import com.google.inject.Module;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup;
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo;
import ru.vyarus.dropwizard.guice.test.GuiceyHooksRule;

/**
 * @author Vyacheslav Rusakov
 * @since 13.04.2018
 */
public class ConfigurationHookDwAppTest {

    static DropwizardAppRule RULE = new DropwizardAppRule<>(ConfigurationHookGuiceyAppTest.App.class);

    @ClassRule
    public static RuleChain chain = RuleChain
            .outerRule(new GuiceyHooksRule((builder) -> builder.modules(new XMod())))
            .around(RULE);

    @Test
    public void checkHook() {
        final GuiceyConfigurationInfo info = InjectorLookup.getInjector(RULE.getApplication()).get()
                .getInstance(GuiceyConfigurationInfo.class);
        Assert.assertTrue(info.getModules().contains(XMod.class));
    }

    public static class App extends Application<Configuration> {

        @Override
        public void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder().build());
        }

        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    public static class XMod implements Module {
        @Override
        public void configure(Binder binder) {
        }
    }
}
