package ru.vyarus.dropwizard.guice.test.client.builder.util;

import javax.ws.rs.core.CacheControl;
import org.glassfish.jersey.message.internal.CacheControlProvider;
import ru.vyarus.dropwizard.guice.test.client.builder.TestRequestConfig;
import ru.vyarus.dropwizard.guice.test.client.util.SourceAwareValue;

import java.text.DateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * Utility to print configuration to string.
 *
 * @author Vyacheslav Rusakov
 * @since 29.09.2025
 */
public final class TestRequestConfigPrinter {

    private TestRequestConfigPrinter() {
    }

    /**
     * @param config request config
     * @return string representation for provided configuration
     */
    public static String print(final TestRequestConfig config) {
        final StringBuilder sb = new StringBuilder(1000);

        if (config.hasConfiguration()) {
            printMap(sb, "Path params", config.getConfiguredPathParamsSource());
            printMap(sb, "Query params", config.getConfiguredQueryParamsSource());
            printMap(sb, "Matrix params", config.getConfiguredMatrixParamsSource());
            printMap(sb, "Headers", config.getConfiguredHeadersSource());
            printMap(sb, "Cookies", config.getConfiguredCookiesSource(), true);
            printMap(sb, "Properties", config.getConfiguredPropertiesSource());
            printClasses(sb, "Extensions", config.getConfiguredExtensionsSource().values());
            printStrings(sb, "Accept", config.getConfiguredAcceptsSource());
            printStrings(sb, "Language", config.getConfiguredLanguagesSource());
            printStrings(sb, "Encoding", config.getConfiguredEncodingsSource());
            printClasses(sb, "Path modifiers", config.getConfiguredPathModifiersSource());
            printClasses(sb, "Request modifiers", config.getConfiguredRequestModifiersSource());

            final SourceAwareValue<CacheControl> cacheControl = config.getConfiguredCacheControlSource();
            if (cacheControl != null) {
                title(sb, "Cache");
                item(sb, new CacheControlProvider().toString(cacheControl.get()), cacheControl.getSource());
            }
            final SourceAwareValue<DateFormat> dateFormatter = config.getConfiguredFormDateFormatterSource();
            if (dateFormatter != null) {
                title(sb, "Custom Date (java.util) formatter");
                item(sb, dateFormatter.get().getClass().getSimpleName(), dateFormatter.getSource());
            }
            final SourceAwareValue<DateTimeFormatter> dateTimeFormatter = config
                    .getConfiguredFormDateTimeFormatterSource();
            if (dateTimeFormatter != null) {
                title(sb, "Custom Date (java.time) formatter");
                item(sb, dateTimeFormatter.get().getClass().getSimpleName(), dateTimeFormatter.getSource());
            }
        } else {
            sb.append("\n\tNo configurations\n");
        }

        return sb.toString();
    }

    private static void printMap(final StringBuilder res, final String title,
                                 final Map<String, ? extends SourceAwareValue<?>> params) {
        printMap(res, title, params, false);
    }
    private static void printMap(final StringBuilder res, final String title,
                                 final Map<String, ? extends SourceAwareValue<?>> params, final boolean skipKey) {
        if (!params.isEmpty()) {
            title(res, title);
            params.forEach((s, o) ->
                    item(res, (skipKey ? "" : (s + "=")) + o.get(), o.getSource()));
        }
    }

    private static void printStrings(final StringBuilder res, final String title,
                                     final SourceAwareValue<String[]> strings) {
        if (strings != null) {
            title(res, title);
            Arrays.stream(strings.get()).forEach(s -> item(res, s, strings.getSource()));
        }
    }

    private static void printClasses(
            final StringBuilder res, final String title, final Collection<? extends SourceAwareValue<?>> objects) {
        if (!objects.isEmpty()) {
            title(res, title);
            objects.forEach(s -> {
                final Object o = s.get();
                final Class<?> aClass = (Class<?>) (o instanceof Class ? o : o.getClass());
                final String name = (aClass.isAnonymousClass() || aClass.isSynthetic())
                        ? "<lambda>" : aClass.getSimpleName();
                item(res, name, s.getSource());
            });
        }
    }

    private static void title(final StringBuilder res, final String title) {
        res.append("\n\t").append(title).append(":\n");
    }

    private static void item(final StringBuilder res, final String item, final String source) {
        res.append("\t\t").append(String.format("%-40s  %s", item, source)).append('\n');
    }
}
