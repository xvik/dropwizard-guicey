package ru.vyarus.dropwizard.guice.test.configurator.unit;

import com.google.inject.Binder;
import com.google.inject.Module;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo;
import ru.vyarus.dropwizard.guice.test.GuiceyAppRule;
import ru.vyarus.dropwizard.guice.test.GuiceyConfiguratorRule;

/**
 * @author Vyacheslav Rusakov
 * @since 13.04.2018
 */
public class ConfigurerGuiceyAppTest {

    static GuiceyAppRule RULE = new GuiceyAppRule<>(App.class, null);

    @ClassRule
    public static RuleChain chain = RuleChain
            .outerRule(new GuiceyConfiguratorRule((builder) -> builder.modules(new XMod())))
            .around(RULE);

    @Test
    public void checkConfigurer() {
        final GuiceyConfigurationInfo info = (GuiceyConfigurationInfo) RULE.getBean(GuiceyConfigurationInfo.class);
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
