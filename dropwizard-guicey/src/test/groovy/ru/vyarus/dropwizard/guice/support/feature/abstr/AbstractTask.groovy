package ru.vyarus.dropwizard.guice.support.feature.abstr

import io.dropwizard.servlets.tasks.Task

/**
 * @author Vyacheslav Rusakov 
 * @since 04.09.2014
 */
abstract class AbstractTask extends Task {

    AbstractTask() {
        super("name")
    }
}
