package ru.vyarus.dropwizard.guice.examples.service;

import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.examples.event.BarEvent;
import ru.vyarus.dropwizard.guice.examples.event.BaseEvent;
import ru.vyarus.dropwizard.guice.examples.event.FooEvent;

/**
 * @author Vyacheslav Rusakov
 * @since 07.03.2017
 */
public class EventListener {

    private Logger logger = LoggerFactory.getLogger(EventListener.class);

    private int base;
    private int foo;
    private int bar;

    @Subscribe
    public void foo(FooEvent event) {
        foo++;
        logger.info("Foo event {} received: {}", foo, event);
    }

    @Subscribe
    public void bar(BarEvent event) {
        bar++;
        logger.info("Bar event {} received: {}", bar, event);
    }

    @Subscribe
    public void base(BaseEvent event) {
        base++;
        logger.info("Base event {} received: {}", base, event);
    }

    public int getBaseCalls() {
        return base;
    }

    public int getFooCalls() {
        return foo;
    }

    public int getBarCalls() {
        return bar;
    }
}
