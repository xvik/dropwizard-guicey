package ru.vyarus.dropwizard.guice.url.resource.debug;

import ru.vyarus.dropwizard.guice.debug.util.RenderUtils;
import ru.vyarus.dropwizard.guice.url.model.ResourceMethodInfo;
import ru.vyarus.dropwizard.guice.url.model.param.BeanParameterSource;
import ru.vyarus.dropwizard.guice.url.model.param.ParameterSource;
import ru.vyarus.java.generics.resolver.GenericsResolver;
import ru.vyarus.java.generics.resolver.util.TypeToStringUtils;
import ru.vyarus.java.generics.resolver.util.map.EmptyGenericsMap;

import java.util.List;

/**
 * Renders resource method analysis report. Identifies what arguments were used, how recognized and how
 * converted.
 * <p>
 * Report build using preserved jersey {@link org.glassfish.jersey.model.Parameter}, representing physical
 * parameters location (method argument, bean param field).
 *
 * @author Vyacheslav Rusakov
 * @since 19.12.2025
 */
public final class CallAnalysisReportBuilder {

    private CallAnalysisReportBuilder() {
    }

    /**
     * Render analysis report.
     *
     * @param info call analysis result object
     * @return rendered report
     */
    public static String buildDebugReport(final ResourceMethodInfo info) {
        return buildDebugReport(info, "\t");
    }

    /**
     * Render analysis report with shift (useful for sub-resources rendering).
     *
     * @param info call analysis result object
     * @param prefix report prefix (space before each line)
     * @return rendered report
     */
    @SuppressWarnings("PMD.UseStringBufferForStringAppends")
    public static String buildDebugReport(final ResourceMethodInfo info, final String prefix) {
        final StringBuilder report = new StringBuilder();
        String tab = prefix;

        if (info.getSteps().isEmpty()) {
            renderStep(info, tab, report, info.getFullPath());
        } else {
            final List<ResourceMethodInfo> steps = info.getSteps();
            for (int i = 0; i < steps.size(); i++) {
                final ResourceMethodInfo step = steps.get(i);
                // Class @Path annotation is ignored for sub resources
                final String path = i == 0 ? step.getFullPath() : step.getPath();
                renderStep(step, tab, report, path);
                tab = tab + "\t";
            }
        }
        report.append('\n');
        return report.toString();
    }

    private static void renderStep(final ResourceMethodInfo info,
                                   final String prefix,
                                   final StringBuilder report,
                                   final String path) {
        report.append('\n').append(prefix);
        // null could be for sub resource locator calls
        if (info.getHttpMethod() != null) {
            report.append(info.getHttpMethod()).append(' ');
        }
        report.append(path);
        if (!info.getProduces().isEmpty()) {
            report.append(" (").append(String.join(", ", info.getProduces())).append(')');
        }
        if (!info.getConsumes().isEmpty()) {
            report.append(" [consumes: ").append(String.join(", ", info.getConsumes())).append(']');
        }
        report.append('\n');

        // in case if method params use generics
        String method = GenericsResolver.resolve(info.getResource()).method(info.getMethod()).toStringMethod();
        // cut off return type
        method = method.substring(method.indexOf(info.getMethod().getName()));
        report.append(prefix)
                .append(info.getResource().getSimpleName())
                .append('.').append(method)
                .append(":\n");

        // skip if no parameters registered (e.g. no-arg method)
        if (!info.selectParameterSources(parameterSource -> true).isEmpty()) {
            renderParameters(info, report, prefix + "\t");
        }
    }

    private static void renderParameters(final ResourceMethodInfo info, final StringBuilder report, final String tab) {
        for (int i = 0; i < info.getMethod().getParameters().length; i++) {
            final int finalI = i;
            final List<ParameterSource> sources = info.selectParameterSources(
                    source -> source.getArgumentPosition() == finalI);
            if (!sources.isEmpty()) {
                boolean first = true;
                for (ParameterSource source : sources) {
                    report.append(tab);
                    if (first) {
                        report.append(String.format("%2s", i + 1)).append("  ");
                        first = false;
                    } else {
                        report.append("    ");
                    }

                    report.append(String.format("%8s ", source.getType()));
                    if (source.isBeanParam()) {
                        final BeanParameterSource beanParam = (BeanParameterSource) source;
                        report.append(String.format(" %-20s ", "("
                                + TypeToStringUtils.toStringType(beanParam.getParameter().getType(),
                                EmptyGenericsMap.getInstance()) + " "
                                + beanParam.getField().getName() + ")"));
                    }
                    report.append("\t ").append(renderValue(info, source)).append('\n');
                }
            }
        }
    }

    private static String renderValue(final ResourceMethodInfo info, final ParameterSource source) {
        // showing actually used value (could be already transformed)
        final String value = String.valueOf(info.getParameterValue(source));
        String res = (source.getName() != null ? source.getName() : "") + " = " + value;
        if (source.getUsedConverter() != null) {
            res += " (converted by " + RenderUtils.getClassName(source.getUsedConverter()) + ")";
        }
        return res;
    }
}
