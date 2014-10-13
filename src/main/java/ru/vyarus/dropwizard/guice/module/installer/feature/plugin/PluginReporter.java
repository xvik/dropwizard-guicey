package ru.vyarus.dropwizard.guice.module.installer.feature.plugin;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import ru.vyarus.dropwizard.guice.module.installer.util.Reporter;

import java.util.Iterator;

import static java.lang.String.format;

/**
 * Special reporter for detailed plugins report.
 *
 * @author Vyacheslav Rusakov
 * @since 12.10.2014
 */
public class PluginReporter extends Reporter {
    private static final String NAMED_KEY = "Map<%s, %s>";
    private static final String NAMED_LINE = TAB + "%-10s (%s)";
    private static final String KEY = "Set<%s>";
    private static final String LINE = TAB + "(%s)";

    private final Multimap<String, String> namedPlugins = HashMultimap.create();
    private final Multimap<String, String> plugins = HashMultimap.create();

    public PluginReporter() {
        super(PluginInstaller.class, "plugins =");
    }

    public PluginReporter named(final Class keyType, final Class extType, final Object key, final Class extension) {
        namedPlugins.put(format(NAMED_KEY, keyType.getSimpleName(), extType.getSimpleName()),
                format(NAMED_LINE, key, extension.getName()));
        return this;
    }

    public PluginReporter simple(final Class extType, final Class extension) {
        plugins.put(format(KEY, extType.getSimpleName()), format(LINE, extension.getName()));
        return this;
    }

    @Override
    public void report() {
        printAll(plugins);
        if (!plugins.isEmpty() && !namedPlugins.isEmpty()) {
            emptyLine();
        }
        printAll(namedPlugins);

        super.report();
    }

    private void printAll(final Multimap<String, String> map) {
        final Iterator<String> it = map.keySet().iterator();
        while (it.hasNext()) {
            final String key = it.next();
            line(key);
            for (String ext : map.get(key)) {
                line(ext);
            }
            if (it.hasNext()) {
                emptyLine();
            }
        }
    }
}
