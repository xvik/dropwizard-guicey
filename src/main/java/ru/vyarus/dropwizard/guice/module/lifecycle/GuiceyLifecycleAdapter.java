package ru.vyarus.dropwizard.guice.module.lifecycle;

import ru.vyarus.dropwizard.guice.module.lifecycle.event.GuiceyLifecycleEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration.ConfigurationHooksProcessedEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration.InitializationEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.hk.HK2ConfigurationEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.hk.HK2ExtensionsInstalledByEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.hk.HK2ExtensionsInstalledEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.run.*;

/**
 * Adapter for {@link GuiceyLifecycleListener}. Useful when you need to listen for multiple events: extend adapter
 * and override just required methods.
 *
 * @author Vyacheslav Rusakov
 * @since 18.04.2018
 */
public class GuiceyLifecycleAdapter implements GuiceyLifecycleListener {

    @Override
    @SuppressWarnings({"checkstyle:CyclomaticComplexity", "checkstyle:MissingSwitchDefault",
            "PMD.NcssCount", "PMD.CyclomaticComplexity", "PMD.SwitchStmtsShouldHaveDefault"})
    public void onEvent(final GuiceyLifecycleEvent event) {
        switch (event.getType()) {
            case ConfigurationHooksProcessed:
                configurationHooksProcessed((ConfigurationHooksProcessedEvent) event);
                break;
            case Initialization:
                initialization((InitializationEvent) event);
                break;
            case BeforeRun:
                beforeRun((BeforeRunEvent) event);
                break;
            case BundlesFromDwResolved:
                dwBundlesResolved((BundlesFromDwResolvedEvent) event);
                break;
            case BundlesFromLookupResolved:
                lookupBundlesResolved((BundlesFromLookupResolvedEvent) event);
                break;
            case BundlesResolved:
                bundlesResolved((BundlesResolvedEvent) event);
                break;
            case BundlesProcessed:
                bundlesProcessed((BundlesProcessedEvent) event);
                break;
            case InjectorCreation:
                injectorCreation((InjectorCreationEvent) event);
                break;
            case InstallersResolved:
                installersResolved((InstallersResolvedEvent) event);
                break;
            case ExtensionsResolved:
                extensionsResolved((ExtensionsResolvedEvent) event);
                break;
            case ExtensionsInstalledBy:
                extensionsInstalledBy((ExtensionsInstalledByEvent) event);
                break;
            case ExtensionsInstalled:
                extensionsInstalled((ExtensionsInstalledEvent) event);
                break;
            case ApplicationRun:
                applicationRun((ApplicationRunEvent) event);
                break;
            case HK2Configuration:
                hk2Configuration((HK2ConfigurationEvent) event);
                break;
            case HK2ExtensionsInstalledBy:
                hk2ExtensionsInstalledBy((HK2ExtensionsInstalledByEvent) event);
                break;
            case HK2ExtensionsInstalled:
                hk2ExtensionsInstalled((HK2ExtensionsInstalledEvent) event);
                break;
        }
    }

    protected void configurationHooksProcessed(final ConfigurationHooksProcessedEvent event) {
        // empty
    }

    protected void initialization(final InitializationEvent event) {
        // empty
    }

    protected void beforeRun(final BeforeRunEvent event) {
        // empty
    }

    protected void dwBundlesResolved(final BundlesFromDwResolvedEvent event) {
        // empty
    }

    protected void lookupBundlesResolved(final BundlesFromLookupResolvedEvent event) {
        // empty
    }

    protected void bundlesResolved(final BundlesResolvedEvent event) {
        // empty
    }

    protected void bundlesProcessed(final BundlesProcessedEvent event) {
        // empty
    }

    protected void injectorCreation(final InjectorCreationEvent event) {
        // empty
    }

    protected void installersResolved(final InstallersResolvedEvent event) {
        // empty
    }

    protected void extensionsResolved(final ExtensionsResolvedEvent event) {
        // empty
    }

    protected void extensionsInstalledBy(final ExtensionsInstalledByEvent event) {
        // empty
    }

    protected void extensionsInstalled(final ExtensionsInstalledEvent event) {
        // empty
    }

    protected void applicationRun(final ApplicationRunEvent event) {
        // empty
    }

    protected void hk2Configuration(final HK2ConfigurationEvent event) {
        // empty
    }

    protected void hk2ExtensionsInstalledBy(final HK2ExtensionsInstalledByEvent event) {
        // empty
    }

    protected void hk2ExtensionsInstalled(final HK2ExtensionsInstalledEvent event) {
        // empty
    }
}
