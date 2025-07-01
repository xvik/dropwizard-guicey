package ru.vyarus.guicey.eventbus.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.guicey.eventbus.service.EventSubscribersInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static ru.vyarus.dropwizard.guice.module.installer.util.Reporter.NEWLINE;
import static ru.vyarus.dropwizard.guice.module.installer.util.Reporter.TAB;

/**
 * Reports subscribed event listeners.
 * Note: it will report only known subscribers (because some beans may be instantiated lazily with guice JIT
 * after server startup).
 *
 * @author Vyacheslav Rusakov
 * @since 02.12.2016
 */
public class EventSubscribersReporter {
    private final Logger logger = LoggerFactory.getLogger(EventSubscribersReporter.class);

    private final EventSubscribersInfo info;

    /**
     * Create event bus subscribers reporter.
     *
     * @param info subscribers info
     */
    public EventSubscribersReporter(final EventSubscribersInfo info) {
        this.info = info;
    }

    /**
     * @return rendered events and subscribers report
     */
    public String renderReport() {
        final Set<Class> events = info.getListenedEvents();
        if (events.isEmpty()) {
            return null;
        }

        final List<Class> sortedEvents = new ArrayList<>(events);
        sortedEvents.sort(Comparator.comparing(Class::getSimpleName));
        final StringBuilder res = new StringBuilder("EventBus subscribers = ")
                .append(NEWLINE);
        for (Class event : sortedEvents) {
            res.append(NEWLINE).append(TAB).append(event.getSimpleName()).append(NEWLINE);
            for (Class subs : info.getListenerTypes(event)) {
                res.append(TAB).append(TAB).append(subs.getName()).append(NEWLINE);
            }
        }
        return res.toString();
    }

    /**
     * Print registered listeners to console. Do nothing if no known listeners.
     */
    public void report() {
        final String report = renderReport();
        if (report != null) {
            logger.info(report);
        }
    }
}
