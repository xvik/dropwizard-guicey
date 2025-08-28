package ru.vyarus.dropwizard.guice.test.util;

import org.junit.jupiter.api.extension.ExtensionContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * Utilities for test console reports formatting.
 *
 * @author Vyacheslav Rusakov
 * @since 12.02.2025
 */
@SuppressWarnings("PMD.GodClass")
public final class PrintUtils {

    private static final String DURATION_FORMATION = "%2.3f %s";
    private static final String VALUE_FORMATION = "%2.3f";

    private PrintUtils() {
    }

    /**
     * This is the same string as shown in the default Object.toString (@hash part).
     *
     * @param object object to get identity for
     * @return object identity string
     */
    public static String identity(final Object object) {
        return Integer.toHexString(System.identityHashCode(object));
    }

    /**
     * Converts arguments to string.
     *
     * @param args      arguments
     * @param maxLength maximum string length to keep (longer strings get truncated)
     * @return string representation of incoming arguments
     */
    public static String[] toStringArguments(final Object[] args, final int maxLength) {
        final String[] arguments = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            arguments[i] = toStringValue(args[i], maxLength);
        }
        return arguments;
    }

    /**
     * Converts any value (object) to string. Rules:
     * - Primitive values, number and booleans stored as is
     * - String values could be truncated
     * - Objects represented as ObjectType@instanceHash
     * - null is "null"
     *
     * @param value     value to convert to string
     * @param maxLength maximum string length to keep
     * @return string representation for the object
     */
    public static String toStringValue(final Object value, final int maxLength) {
        if (value == null) {
            return "null";
        }
        String res;
        if (value instanceof String) {
            res = (String) value;
            if (res.length() > maxLength) {
                res = res.substring(0, maxLength) + "...";
            }
        } else if (value.getClass().isPrimitive() || value instanceof Number || value instanceof Boolean) {
            res = value.toString();
        } else if (value instanceof Collection) {
            res = toStringCollection((Collection<?>) value, maxLength);
        } else if (value.getClass().isArray()) {
            res = toStringArray((Object[]) value, maxLength);
        } else {
            res = value.getClass().getSimpleName() + "@" + identity(value);
        }
        return res;
    }

    /**
     * Render duration together with increase indication: overall time (increase since last report).
     *
     * @param overall  overall duration (including increase) (could be null)
     * @param increase increase since last report (could be null)
     * @return string representation for suration
     */
    public static String renderTime(final Duration overall, final Duration increase) {
        return overall == null ? "--" : (ms(overall)
                + (increase != null && increase.toNanos() > 0 ? (" ( + " + ms(increase) + ")") : ""));
    }

    /**
     * Render duration in milliseconds (2 decimal signs precision).
     *
     * @param duration duration
     * @return string representation
     */
    public static String ms(final Duration duration) {
        return ms(duration.toNanos());
    }

    /**
     * @param nanos time in nanoseconds
     * @return string representation
     */
    public static String ms(final long nanos) {
        final long millis = Math.round((double) nanos / 1_000_000);
        final String res;
        if (millis > 10) {
            res = millis + " ms";
        } else if (nanos < 10) {
            res = "0.00 ms";
        } else if (nanos < 100) {
            // bigger precision for too small numbers (to avoid confusion by showing raw nanos)
            res = formatMs(nanos, 5);
        } else if (nanos < 1000) {
            res = formatMs(nanos, 4);
        } else if (nanos < 10_000) {
            res = formatMs(nanos, 3);
        } else {
            res = formatMs(nanos, 2);
        }
        return res;
    }

    /**
     * Format nano value in milliseconds with required precision (decimal numbers).
     *
     * @param nanos     value in nanoseconds
     * @param precision required precision (decimal numbers)
     * @return formatted value
     */
    public static String formatMs(final long nanos, final int precision) {
        final String format = "%." + precision + "f ms";
        return String.format(format, new BigDecimal(nanos)
                .divide(BigDecimal.valueOf(1_000_000), precision, RoundingMode.HALF_UP)
                .doubleValue());
    }

    /**
     * Time assumed to come from metrics snapshot.
     *
     * @param time time to format
     * @return string representation (in ms)
     */
    public static String formatMetric(final double time) {
        return formatMetric(time, TimeUnit.MILLISECONDS);
    }

    /**
     * Formats time, obtained from metrics snapshot to target unit.
     *
     * @param time time to format
     * @param unit target unit
     * @return string representation (in selected unit)
     */
    public static String formatMetric(final double time, final TimeUnit unit) {
        if (unit == null) {
            return String.format(VALUE_FORMATION, time);
        } else {
            return String.format(DURATION_FORMATION, convertDuration(time, unit), toStringUnit(unit));
        }
    }

    /**
     * Universal header string for performance reports in test to clearly identify current context.
     *
     * @param context junit context
     * @return performance report header
     */
    public static String getPerformanceReportSeparator(final ExtensionContext context) {
        String inst = "---------------------------------\n";
        if (context.getTestInstance().isPresent()) {
            inst = "/ test instance = " + identity(context.getTestInstance().get()) + " /\n";
        }
        return "\n\\\\\\------------------------------------------------------------" + inst;
    }

    private static String toStringCollection(final Collection<?> list, final int maxLength) {
        final StringBuilder builder = new StringBuilder("(").append(list.size()).append(")[");
        if (!list.isEmpty()) {
            builder.append(' ');
        }
        final Iterator<?> it = list.iterator();
        int cnt = 0;
        // show up to 10 values
        while (it.hasNext() && cnt < 10) {
            builder.append(cnt > 0 ? "," : "").append(toStringValue(it.next(), maxLength));
            cnt++;
        }
        if (list.size() > 10) {
            builder.append(",...");
        }
        if (!list.isEmpty()) {
            builder.append(' ');
        }

        return builder.append(']').toString();
    }

    private static String toStringArray(final Object[] array, final int maxLength) {
        return toStringCollection(Arrays.asList(array), maxLength);
    }


    @SuppressWarnings("PMD.ExhaustiveSwitchHasDefault")
    private static String toStringUnit(final TimeUnit unit) {
        final String res;
        switch (unit) {
            case NANOSECONDS:
                res = "ns";
                break;
            case MICROSECONDS:
                res = "μs";
                break;
            case MILLISECONDS:
                res = "ms";
                break;
            case SECONDS:
                res = "s";
                break;
            case MINUTES:
                res = "m";
                break;
            case HOURS:
                res = "h";
                break;
            case DAYS:
                res = "d";
                break;
            default:
                res = unit.toString().toLowerCase();
                break;
        }
        return res;
    }

    private static double convertDuration(final double duration, final TimeUnit durationUnit) {
        final double durationFactor = 1.0 / durationUnit.toNanos(1);
        return duration * durationFactor;
    }
}
