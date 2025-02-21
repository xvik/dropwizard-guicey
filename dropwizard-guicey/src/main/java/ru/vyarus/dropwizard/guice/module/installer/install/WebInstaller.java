package ru.vyarus.dropwizard.guice.module.installer.install;

/**
 * Marker interface for installers, installing web objects (servlets, filters, rest resources).
 * All installers of features available only with complete application start must be indicated as web installers
 * (those extensions that are ignored when lightweight guicey test
 * ({@link ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp}) started).
 * <p>
 * All {@link ru.vyarus.dropwizard.guice.module.installer.install.JerseyInstaller} related installers are
 * already marked as web.
 *
 * @author Vyacheslav Rusakov
 * @since 20.02.2025
 */
public interface WebInstaller {
}
