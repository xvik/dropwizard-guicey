package ru.vyarus.dropwizard.guice.test.client.builder.util.conf;

import com.fasterxml.jackson.databind.util.StdDateFormat;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Convers form parameters into strings. With a special support for dates conversion.
 *
 * @author Vyacheslav Rusakov
 * @since 15.09.2025
 */
public final class FormParamsSupport {

    /**
     * Default java.util dates formatter.
     */
    // StdDateFormat is thread safe
    @SuppressFBWarnings("STCAL_STATIC_SIMPLE_DATE_FORMAT_INSTANCE")
    public static final DateFormat DEFAULT_DATE_FORMAT = new StdDateFormat()
            .withTimeZone(TimeZone.getTimeZone("UTC"))
            .withColonInTimeZone(true);

    /**
     * Default java.time dates formatter.
     */
    public static final DateTimeFormatter DEFAULT_DATE_TIME_FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private FormParamsSupport() {
    }

    /**
     * Format the given parameter object into string.
     * <p>
     * String conversion specifics:
     * <ul>
     *     <li>Date fields string conversion could be customized with date formatters (one for java.util and other for
     *        java.time api).</li>
     *     <li>Null values converted to ""</li>
     *     <li>Collections and arrays converted to a comma-separated string of values (converted to string)</li>
     *     <li>By default, call toString on provided object</li>
     * </ul>
     * <p>
     * To customize date fields conversion use
     * {@link #parameterToString(Object, java.text.DateFormat, java.time.format.DateTimeFormatter)}.
     *
     * @param param Object
     * @return Object in string format
     */
    public static String parameterToString(final Object param) {
        return parameterToString(param, null, null);
    }

    /**
     * Format the given parameter object into string.
     * <p>
     * String conversion specifics:
     * <ul>
     *     <li>Date fields string conversion could be customized with date formatters (one for java.util and other for
     *        java.time api).</li>
     *     <li>Null values converted to ""</li>
     *     <li>Collections and arrays converted to a comma-separated string of values (converted to string)</li>
     *     <li>By default, call toString on provided object</li>
     * </ul>
     *
     * @param param          Object
     * @param dateFormat     java util date formatter
     * @param dateTimeFormat java time date formatter
     * @return Object in string format
     */
    @SuppressWarnings("checkstyle:ReturnCount")
    public static String parameterToString(final Object param,
                                           final @Nullable DateFormat dateFormat,
                                           final @Nullable DateTimeFormatter dateTimeFormat) {
        final DateFormat format1 = dateFormat == null ? DEFAULT_DATE_FORMAT : dateFormat;
        final DateTimeFormatter format2 = dateTimeFormat == null ? DEFAULT_DATE_TIME_FORMAT : dateTimeFormat;

        if (param == null) {
            return "";
            // date, timestamp
        } else if (param instanceof Date) {
            return format1.format((Date) param);
            // localdate, offsetdatetime etc.
        } else if (param instanceof TemporalAccessor) {
            return format2.format((TemporalAccessor) param);
        } else if (param instanceof Collection<?>) {
            final StringBuilder b = new StringBuilder();
            ((Collection<?>) param).forEach(val -> {
                if (!b.isEmpty()) {
                    b.append(',');
                }
                b.append(parameterToString(val));
            });
            return b.toString();
        } else if (param.getClass().isArray()) {
            final int length = Array.getLength(param);
            final List<Object> params = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                params.add(Array.get(param, i));
            }
            return parameterToString(params, format1, format2);
        } else {
            return String.valueOf(param);
        }
    }
}
