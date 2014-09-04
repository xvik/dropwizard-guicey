package ru.vyarus.dropwizard.guice.module.installer.feature.health;

import com.codahale.metrics.health.HealthCheck;

/**
 * Base class for automatic health check registration.
 * Additional class required, because check must be registered with name, not available in base healthcheck.
 */
public abstract class NamedHealthCheck extends HealthCheck {

    /**
     * @return health check name
     */
    public abstract String getName();
}
