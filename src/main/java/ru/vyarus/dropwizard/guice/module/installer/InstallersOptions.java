package ru.vyarus.dropwizard.guice.module.installer;

import ru.vyarus.dropwizard.guice.module.context.option.Option;

/**
 * Bundled installers options. Applies for both {@link CoreInstallersBundle} and {@link WebInstallersBundle}
 * installers.
 */
public enum InstallersOptions implements Option {

    /**
     * Affects {@link ru.vyarus.dropwizard.guice.module.installer.feature.web.WebServletInstaller}.
     * During servlet registration, url patterns may clash with already installed servlets. By default, only warning
     * will be printed in log. Set option to {@code true} to throw exception on registration if clash detected.
     * <p>
     * Note: clash resolution depends on servlets registration order. Moreover, clash may appear on some 3rd party
     * servlet registration (not managed by installer) and so be not affected by this option.
     */
    DenyServletRegistrationWithClash(Boolean.class, false),

    /**
     * Affects {@link ru.vyarus.dropwizard.guice.module.installer.feature.web.listener.WebListenerInstaller}.
     * By default, dropwizard does not have configured sessions support (to be stateless), so session listeners
     * can't be installed.
     * Because session listeners may be defined as part of 3rd party bundles and most likely will complement bundle
     * functionality (aka optional part), listener installer will only log warning about not installed listeners.
     * Set option to {@code true} to throw error instead (when session listeners can't be installed because of no
     * sessions support enabled).
     */
    DenySessionListenersWithoutSession(Boolean.class, false);

    private Class<?> type;
    private Object value;

    <T> InstallersOptions(final Class<T> type, final T value) {
        this.type = type;
        this.value = value;
    }


    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public Object getDefaultValue() {
        return value;
    }
}
