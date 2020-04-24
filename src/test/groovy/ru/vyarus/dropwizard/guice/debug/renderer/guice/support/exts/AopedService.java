package ru.vyarus.dropwizard.guice.debug.renderer.guice.support.exts;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Vyacheslav Rusakov
 * @since 14.09.2019
 */
@Singleton
public class AopedService {
    @Inject
    JitService service;
}
