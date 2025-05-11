package ru.vyarus.dropwizard.guice.debug.report.start;

import com.google.common.base.MoreObjects;
import ru.vyarus.dropwizard.guice.debug.util.RenderUtils;
import ru.vyarus.dropwizard.guice.module.context.stat.DetailStat;
import ru.vyarus.dropwizard.guice.test.util.PrintUtils;

import java.time.Duration;

/**
 * Render shutdown time report. Could only show stopping-stop lifecycle time and managed objects stop method call.
 *
 * @author Vyacheslav Rusakov
 * @since 10.03.2025
 */
public class ShutdownTimeRenderer {

    /**
     * Render shutdown report.
     *
     * @param info shutdown times
     * @return rendered report
     */
    public String render(final ShutdownTimeInfo info) {
        final StringBuilder res = new StringBuilder(200);
        res.append("\n\n").append(line(1, "Application shutdown", info.getStopTime()));

        info.getManagedTimes().forEach((type, duration) -> {
            res.append(tab(2)).append(String.format("%-10s", info.getManagedTypes().get(type)))
                    .append(line(0, RenderUtils.getClassName(type), duration));
        });

        res.append(line(2, "Listeners time", info.getListenersTime()));
        info.getStats().getDetailedStats(DetailStat.Listener).forEach((type, time) -> {
            if (info.getEvents().contains(type)) {
                res.append(line(3, type.getSimpleName(), time));
            }
        });

        return res.toString();
    }

    private String line(final int shift,
                        final String name,
                        final Duration duration) {
        return line(shift, name, null, duration, null);
    }

    private String line(final int shift,
                        final String name,
                        final String prefix,
                        final Duration duration,
                        final String postfix) {
        return tab(shift) + format(name, prefix, duration, postfix);
    }

    private String format(final String name, final String prefix, final Duration duration, final String postfix) {
        return String.format("%-35s: %s%s%s%n", name, MoreObjects.firstNonNull(prefix, ""),
                PrintUtils.ms(duration), MoreObjects.firstNonNull(postfix, ""));
    }

    private String tab(final int shift) {
        final StringBuilder res = new StringBuilder();
        for (int i = 0; i < shift; i++) {
            res.append('\t');
        }
        return res.toString();
    }
}
