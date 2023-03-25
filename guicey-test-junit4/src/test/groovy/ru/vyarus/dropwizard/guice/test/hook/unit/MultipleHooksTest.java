package ru.vyarus.dropwizard.guice.test.hook.unit;

import com.google.inject.Binder;
import com.google.inject.Module;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo;
import ru.vyarus.dropwizard.guice.test.GuiceyAppRule;
import ru.vyarus.dropwizard.guice.test.GuiceyHooksRule;

import java.util.Arrays;

/**
 * @author Vyacheslav Rusakov
 * @since 13.04.2018
 */
public class MultipleHooksTest extends BaseTest {

    static GuiceyAppRule RULE = new GuiceyAppRule<>(ConfigurationHookGuiceyAppTest.App.class, null);

    @ClassRule
    public static RuleChain chain = RuleChain
            .outerRule(BASE_CONF)
            .around(new GuiceyHooksRule((builder) -> builder.modules(new XMod2())))
            .around(RULE);


    @Test
    public void checkHook() {
        final GuiceyConfigurationInfo info = (GuiceyConfigurationInfo) RULE.getBean(GuiceyConfigurationInfo.class);
        Assert.assertTrue(info.getModules()
                .containsAll(Arrays.asList(XMod.class, XMod2.class)));
    }

    public static class XMod2 implements Module {
        @Override
        public void configure(Binder binder) {
        }
    }
}
