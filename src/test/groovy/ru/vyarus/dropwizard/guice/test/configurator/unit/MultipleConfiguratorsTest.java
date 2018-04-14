package ru.vyarus.dropwizard.guice.test.configurator.unit;

import com.google.inject.Binder;
import com.google.inject.Module;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo;
import ru.vyarus.dropwizard.guice.test.GuiceyAppRule;
import ru.vyarus.dropwizard.guice.test.GuiceyConfiguratorRule;

import java.util.Arrays;

/**
 * @author Vyacheslav Rusakov
 * @since 13.04.2018
 */
public class MultipleConfiguratorsTest extends BaseTest {

    static GuiceyAppRule RULE = new GuiceyAppRule<>(ConfigurerGuiceyAppTest.App.class, null);

    @ClassRule
    public static RuleChain chain = RuleChain
            .outerRule(BASE_CONF)
            .around(new GuiceyConfiguratorRule((builder) -> builder.modules(new XMod2())))
            .around(RULE);


    @Test
    public void checkConfigurer() {
        final GuiceyConfigurationInfo info = (GuiceyConfigurationInfo) RULE.getBean(GuiceyConfigurationInfo.class);
        Assert.assertTrue(info.getModules()
                .containsAll(Arrays.asList(BaseTest.XMod.class, XMod2.class)));
    }

    public static class XMod2 implements Module {
        @Override
        public void configure(Binder binder) {
        }
    }
}
