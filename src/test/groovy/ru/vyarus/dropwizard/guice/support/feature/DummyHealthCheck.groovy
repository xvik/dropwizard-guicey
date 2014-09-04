package ru.vyarus.dropwizard.guice.support.feature

import ru.vyarus.dropwizard.guice.module.installer.feature.health.NamedHealthCheck

/**
 * @author Vyacheslav Rusakov 
 * @since 03.09.2014
 */
class DummyHealthCheck extends NamedHealthCheck {

    @Override
    String getName() {
        return "sample check"
    }

    @Override
    protected com.codahale.metrics.health.HealthCheck.Result check() throws Exception {
        return Result.healthy()
    }
}
