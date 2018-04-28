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
    DenySessionListenersWithoutSession(Boolean.class, false),
    /**
     * By default, HK related extensions like resources or other jersey specific extensions are managed by
     * guice (guice-managed instance is registered in hk). This makes some hk-specific features not possible
     * (like context injection with @Context annotation).
     * {@link ru.vyarus.dropwizard.guice.module.installer.feature.jersey.HK2Managed} annotation could switch
     * annotated beans to be managed by hk. But in some cases, it is more convenient to always use hk and this
     * option is supposed to be used exactly for such cases.
     * <p>
     * When false value set, all beans, managed by jersey installers
     * ({@link ru.vyarus.dropwizard.guice.module.installer.install.JerseyInstaller}) should register beans for
     * hk management. {@link ru.vyarus.dropwizard.guice.module.installer.feature.jersey.HK2Managed} become
     * useless in this case, instead {@link ru.vyarus.dropwizard.guice.module.installer.feature.jersey.GuiceManaged}
     * annotation could be used.
     * <p>
     * NOTE: guice aop is not applicable for beans managed by hk (because guice aop use class proxies and not
     * instance proxies).
     * <p>
     * Startup will fail if hk bridge is not enabled
     * (see {@link ru.vyarus.dropwizard.guice.GuiceyOptions#UseHkBridge}) because without it you can't inject
     * any guice beans into hk managed instances (and if you don't need to then you don't need guice support at all).
     */
    HkExtensionsManagedByGuice(Boolean.class, true);

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
