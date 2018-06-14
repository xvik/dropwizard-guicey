package ru.vyarus.dropwizard.guice.module.lifecycle;

import ru.vyarus.dropwizard.guice.module.lifecycle.event.GuiceyLifecycleEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration.ConfiguratorsProcessedEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration.InitializationEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.hk.HkConfigurationEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.hk.HkExtensionsInstalledByEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.hk.HkExtensionsInstalledEvent;
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
            case ConfiguratorsProcessed:
                configuratorsProcessed((ConfiguratorsProcessedEvent) event);
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
            case HkConfiguration:
                hkConfiguration((HkConfigurationEvent) event);
                break;
            case HkExtensionsInstalledBy:
                hkExtensionsInstalledBy((HkExtensionsInstalledByEvent) event);
                break;
            case HkExtensionsInstalled:
                hkExtensionsInstalled((HkExtensionsInstalledEvent) event);
                break;
        }
    }

    protected void configuratorsProcessed(final ConfiguratorsProcessedEvent event) {
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

    protected void hkConfiguration(final HkConfigurationEvent event) {
        // empty
    }

    protected void hkExtensionsInstalledBy(final HkExtensionsInstalledByEvent event) {
        // empty
    }

    protected void hkExtensionsInstalled(final HkExtensionsInstalledEvent event) {
        // empty
    }
}
