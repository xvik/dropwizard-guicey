package ru.vyarus.dropwizard.guice.debug.report.start;

import ru.vyarus.dropwizard.guice.debug.util.RenderUtils;
import ru.vyarus.dropwizard.guice.test.util.PrintUtils;

import java.time.Duration;

/**
 * Render shutdown time report. Could only show stopping-stop lifecycle time and managed objects stop method call.
 *
 * @author Vyacheslav Rusakov
 * @since 10.03.2025
 */
public class ShutdownTimeRenderer {

    public String render(final ShutdownTimeInfo info) {
        final StringBuilder res = new StringBuilder(200);
        res.append("\n\n\t").append(format("Application shutdown", info.getStopTime()));

        info.getManagedTimes().forEach((type, duration) -> {
            res.append(String.format("\t\t%-10s", info.getManagedTypes().get(type)))
                    .append(format(RenderUtils.getClassName(type), duration));
        });

        return res.toString();
    }

    private String format(final String name, final Duration duration) {
        return String.format("%-35s: %s%n", name, PrintUtils.ms(duration));
    }
}
