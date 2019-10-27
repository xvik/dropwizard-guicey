package ru.vyarus.dropwizard.guice.debug.report.jersey;

import org.glassfish.jersey.internal.inject.InjectionManager;
import ru.vyarus.dropwizard.guice.debug.report.ReportRenderer;
import ru.vyarus.dropwizard.guice.debug.report.jersey.util.ProviderRenderUtil;
import ru.vyarus.dropwizard.guice.module.installer.install.binding.LazyBinding;
import ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding;

import java.util.List;

import static ru.vyarus.dropwizard.guice.module.installer.util.Reporter.NEWLINE;
import static ru.vyarus.dropwizard.guice.module.installer.util.Reporter.TAB;

/**
 * Jersey configuration report renderer.
 *
 * @author Vyacheslav Rusakov
 * @since 26.10.2019
 */
public class JerseyConfigRenderer implements ReportRenderer<JerseyConfig> {

    private final InjectionManager manager;
    private final boolean guiceFirstMode;

    public JerseyConfigRenderer(final InjectionManager manager, final boolean guiceFirstMode) {
        this.manager = manager;
        this.guiceFirstMode = guiceFirstMode;
    }

    @Override
    public String renderReport(final JerseyConfig config) {
        final StringBuilder res = new StringBuilder(NEWLINE).append(NEWLINE);
        for (Class<?> ext : config.getRequiredTypes()) {
            renderGroup(ext, res);
        }
        return res.toString();
    }

    @SuppressWarnings("unchecked")
    private void renderGroup(final Class<?> ext, final StringBuilder res) {
        final List providers = manager.getAllInstances(ext);
        if (providers.isEmpty()) {
            return;
        }
        res.append(TAB).append(ProviderRenderUtil.getTypeName(ext)).append(NEWLINE);
        providers.forEach(it -> {
            final Class<?> type = it.getClass();
            final boolean hkManaged = JerseyBinding.isJerseyManaged(type, guiceFirstMode);
            final boolean lazy = type.isAnnotationPresent(LazyBinding.class);
            res.append(TAB).append(TAB)
                    .append(ProviderRenderUtil.render(ext, it, hkManaged, lazy))
                    .append(NEWLINE);
        });
        res.append(NEWLINE);
    }
}
