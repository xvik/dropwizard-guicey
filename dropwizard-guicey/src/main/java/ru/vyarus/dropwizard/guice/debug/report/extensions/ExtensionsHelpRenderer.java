package ru.vyarus.dropwizard.guice.debug.report.extensions;

import ru.vyarus.dropwizard.guice.debug.report.ReportRenderer;
import ru.vyarus.dropwizard.guice.debug.util.RenderUtils;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;

import java.util.List;

import static ru.vyarus.dropwizard.guice.module.installer.util.Reporter.NEWLINE;
import static ru.vyarus.dropwizard.guice.module.installer.util.Reporter.TAB;

/**
 * Renders known extension signs. Signs grouped by installer. Installers ordered in execution order.
 * <p>
 * Note: correct information could be shown only for installers explicitly providing this information
 * (by overriding {@link ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller#getRecognizableSigns()}).
 *
 * @author Vyacheslav Rusakov
 * @since 06.12.2022
 */
public class ExtensionsHelpRenderer implements ReportRenderer<Void> {
    private final List<FeatureInstaller> installers;

    /**
     * Create renderer.
     *
     * @param installers installers
     */
    public ExtensionsHelpRenderer(final List<FeatureInstaller> installers) {
        this.installers = installers;
    }

    @Override
    @SuppressWarnings("unchecked")
    public String renderReport(final Void config) {
        final StringBuilder res = new StringBuilder(NEWLINE);
        for (FeatureInstaller installer : installers) {
            final Class<FeatureInstaller> instType = (Class<FeatureInstaller>) installer.getClass();
            res.append(NEWLINE).append(TAB).append(RenderUtils.renderInstaller(instType, null)).append(NEWLINE);
            final List<String> signs = installer.getRecognizableSigns();
            for (String sign : signs) {
                res.append(TAB).append(TAB).append(sign).append(NEWLINE);
            }
        }
        return res.toString();
    }
}
