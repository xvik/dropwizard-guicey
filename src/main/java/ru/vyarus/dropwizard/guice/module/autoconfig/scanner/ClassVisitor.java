package ru.vyarus.dropwizard.guice.module.autoconfig.scanner;

/**
 * Visitor for classpath scanner.
 *
 * @author Vyacheslav Rusakov
 * @since 01.09.2014
 */
public interface ClassVisitor {

    /**
     * Called for every class found by classpath scanner (but avoiding classes annotated with
     * {@code @InvisibleForScanner}).
     *
     * @param type type to investigate
     */
    void visit(final Class<?> type);
}
