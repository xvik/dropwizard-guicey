package ru.vyarus.dropwizard.guice.module.installer.option;

import ru.vyarus.dropwizard.guice.module.context.option.Options;

/**
 * Marker interface for installers requiring options. {@link ru.vyarus.dropwizard.guice.module.context.option.Options}
 * instance will be set before any installer method call.
 * <p>
 * Installer could extend {@link InstallerOptionsSupport} instead of directly implementing interface
 * (to avid boilerplate).
 *
 * @author Vyacheslav Rusakov
 * @since 18.08.2016
 */
public interface WithOptions {

    /**
     * Called before any installer method.
     *
     * @param options options accessor instance
     */
    void setOptions(Options options);

}
