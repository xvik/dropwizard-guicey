package ru.vyarus.dropwizard.guice.module.context.debug.report.option;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo;
import ru.vyarus.dropwizard.guice.module.context.debug.report.ReportRenderer;
import ru.vyarus.dropwizard.guice.module.context.debug.util.RenderUtils;
import ru.vyarus.dropwizard.guice.module.context.option.OptionsInfo;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;

import static ru.vyarus.dropwizard.guice.module.installer.util.Reporter.NEWLINE;
import static ru.vyarus.dropwizard.guice.module.installer.util.Reporter.TAB;

/**
 * Renders used options. Options are grouped by enum and sorted by enum name.
 * Not used options are not shown (option wasn't set or get).
 * <p>
 * Used markers:
 * <ul>
 * <li>CUSTOM - user override default value</li>
 * <li>NOT_USED - value was set by user but never read (note that it means only that option is not used "for now"
 * because all options are accessible from guice during application lifetime and may be accessed lazily.</li>
 * </ul>
 * <p>
 * Array and iterable values are rendered as: [val1, val2, ...]. Null value rendered as "null". In all other
 * cases toString used. So if custom formatting required for your custom option object - define proper toString.
 *
 * @author Vyacheslav Rusakov
 * @since 13.08.2016
 */
@Singleton
public class OptionsRenderer implements ReportRenderer<Boolean> {

    private static final String POSTFIX = "Options";

    private final GuiceyConfigurationInfo info;

    @Inject
    public OptionsRenderer(final GuiceyConfigurationInfo info) {
        this.info = info;
    }

    /**
     * Renders options report.
     *
     * @param showNotUsedMarker true to show NOT_USED marker for set but never read options, false to avoid marker
     * @return rendered report
     */
    @Override
    public String renderReport(final Boolean showNotUsedMarker) {
        final StringBuilder res = new StringBuilder();
        render(showNotUsedMarker, res);
        return res.toString();
    }

    private void render(final boolean showNotUsedMarker, final StringBuilder res) {
        final List<Class<Enum>> groups = info.getOptions().getOptionGroups();
        for (Class<Enum> group : groups) {
            res.append(NEWLINE).append(NEWLINE).append(TAB)
                    .append(String.format("%-25s (%s)", groupName(group), RenderUtils.renderClass(group)))
                    .append(NEWLINE);
            renderOptions(group, showNotUsedMarker, res);
        }
    }

    @SuppressWarnings("unchecked")
    private void renderOptions(final Class<Enum> group, final boolean showNotUsedMarker, final StringBuilder res) {
        final List<String> markers = Lists.newArrayList();
        for (Enum option : group.getEnumConstants()) {
            final OptionsInfo options = info.getOptions();
            if (options.knowsOption(option)) {
                markers.clear();
                if (options.isSet(option)) {
                    markers.add("CUSTOM");
                }
                if (!options.isUsed(option) && showNotUsedMarker) {
                    markers.add("NOT_USED");
                }
                res.append(TAB).append(TAB)
                        .append(String.format("%-30s = %-30s %s", option.name(),
                                valueToString(options.getValue(option)), RenderUtils.markers(markers))).append(NEWLINE);
            }
        }
    }

    private String groupName(final Class<Enum> group) {
        String name = group.getSimpleName();
        if (name.endsWith(POSTFIX)) {
            name = name.substring(0, name.length() - POSTFIX.length());
        }
        return name;
    }

    private String valueToString(final Object value) {
        final String res;
        if (value == null) {
            res = "null";
        } else {
            final Class type = value.getClass();
            if (type.isArray()) {
                res = Arrays.deepToString((Object[]) value);
            } else if (Iterable.class.isAssignableFrom(type)) {
                res = '[' + Joiner.on(", ").join((Iterable) value) + ']';
            } else {
                res = value.toString();
            }
        }
        return res;
    }
}
