package ru.vyarus.dropwizard.guice.test.jupiter.ext;

import com.google.common.base.Preconditions;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import ru.vyarus.dropwizard.guice.test.TestCommand;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.util.ConfigOverrideUtils;
import ru.vyarus.dropwizard.guice.test.util.HooksUtil;

/**
 * {@link TestGuiceyApp} junit 5 extension implementation.
 *
 * @author Vyacheslav Rusakov
 * @since 29.04.2020
 */
public class GuiceyAppJupiterExtension extends DwExtensionsSupport {

    @Override
    protected DropwizardTestSupport<?> prepareTestSupport(final ExtensionContext context) {
        final TestGuiceyApp cfg = context.getElement().get().getAnnotation(TestGuiceyApp.class);
        // catch incorrect usage by direct @ExtendWith(...)
        Preconditions.checkNotNull(cfg, "%s annotation not declared: can't work without configuration, "
                        + "so either use annotation or extension with @%s for manual configuration",
                TestGuiceyApp.class.getSimpleName(),
                RegisterExtension.class.getSimpleName());

        HooksUtil.register(cfg.hooks());
        return create(cfg.value(), cfg.config(), ConfigOverrideUtils.convert(cfg.configOverride()));
    }

    @SuppressWarnings("unchecked")
    private <C extends Configuration> DropwizardTestSupport<C> create(
            final Class<? extends Application> app,
            final String configPath,
            final ConfigOverride... overrides) {
        return new DropwizardTestSupport<C>((Class<? extends Application<C>>) app,
                configPath,
                (String) null,
                TestCommand::new,
                overrides);
    }
}
