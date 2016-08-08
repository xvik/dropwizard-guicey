package ru.vyarus.dropwizard.guice.module.installer.feature.web.listener;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import ru.vyarus.dropwizard.guice.module.installer.util.Reporter;

import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestAttributeListener;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionIdListener;
import javax.servlet.http.HttpSessionListener;
import java.util.EventListener;
import java.util.Map;

/**
 * Special reporter to build web listeners report.
 *
 * @author Vyacheslav Rusakov
 * @since 09.08.2016
 */
public class ListenerReporter extends Reporter {

    private static final Map<Class<? extends EventListener>, String> DESCRIPTORS =
            ImmutableMap.<Class<? extends EventListener>, String>builder()
                    .put(ServletContextListener.class, "Context")
                    .put(ServletContextAttributeListener.class, "Context attribute")
                    .put(ServletRequestListener.class, "Request")
                    .put(ServletRequestAttributeListener.class, "Request attribute")
                    .put(HttpSessionListener.class, "Session")
                    .put(HttpSessionAttributeListener.class, "Session attribute")
                    .put(HttpSessionIdListener.class, "Session id")
                    .build();

    private final Multimap<String, String> prerender = HashMultimap.create();

    public ListenerReporter() {
        super(WebListenerInstaller.class, "web listeners = ");
    }

    @SuppressWarnings("unchecked")
    public void listener(final Class<? extends EventListener> type, final String contextMarkers) {
        final String line = String.format(TAB + "%-2s  (%s)", contextMarkers, type.getName());
        for (Map.Entry<Class<? extends EventListener>, String> entry : DESCRIPTORS.entrySet()) {
            final Class ext = entry.getKey();
            if (ext.isAssignableFrom(type)) {
                prerender.put(entry.getValue(), line);
            }
        }
    }

    @Override
    public void report() {
        for (String group : prerender.keySet()) {
            separate();
            line(group);
            prerender.get(group).forEach(this::line);
        }
        super.report();
    }
}
