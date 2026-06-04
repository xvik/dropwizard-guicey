# Guicey lifecycle events

Guicey broadcasts lifecycle events at all major points. Each event
provides access to all available state at that point.

Events could be used for configuration analysis, reporting, or to add some special
post-processing for configuration items (e.g. post-process modules before injector creation).

!!! important
    Event listeners cannot modify the configuration itself
    (can't add new extensions, installers, bundles, or disable anything).

## Events

All events are listed in the `GuiceyLifecycle` enum (in execution order).


Event | Description                                                                                                                                                                                                                                                                                                                                                                           | Possible usage
------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------
**Dropwizard initialization phase** |                                                                                                                                                                                                                                                                                                                                                                                       |
ConfigurationHooksProcessed^**?**^ | Called after all registered hooks are processed. Not called when no hooks are used.                                                                                                                                                                                                                                                                                                          | Only for info
DropwizardBundlesInitialized^**?**^ | Called after Dropwizard bundle initialization (for Dropwizard bundles registered through the Guicey API). Not called if no bundles were registered.                                                                                                                                                                                                                                      | Logging, bundle instances modification (to affect run method)
BundlesFromLookupResolved^**?**^ | Called after bundle resolution through the lookup mechanism. Not called if no bundles are found.                                                                                                                                                                                                                                                                                             | Logging or post processing of found bundles.
BundlesResolved | Called with all known top-level bundles (transitive bundles are not yet known). Always called to indicate configuration state.                                                                                                                                                                                                                                                        | Could be used to modify top-level bundle instances
BundlesInitialized^**?**^ | Called after all bundle initialization (including transitive, so the list of bundles could be bigger). Not called when no bundles are registered.                                                                                                                                                                                                                                            | Logging, post processing
CommandsResolved^**?**^ | Called if commands search is enabled and at least one command found                                                                                                                                                                                                                                                                                                                   | Logging
InstallersResolved | Called when all configured (and classpath-scanned) installers are initialized                                                                                                                                                                                                                                                                                                    | Potentially could be used to configure installer instances
ManualExtensionsValidated^**?**^ | Called when all manually registered extension classes are recognized by installers (validated). But only extensions, known to be enabled at that time are actually validated (this way it is possible to exclude extensions for non-existing installers). Called only if at least one manual extension registered.                                                                    | Logging, assertions
ClasspathExtensionsResolved^**?**^ | Called when classes from the classpath scan are analyzed and all extensions are detected (if extension is also registered manually it would be also counted as from classpath scan). Called only if classpath scan is enabled and at least one extension detected.                                                                                                                                | Logging, assertions
Initialized | Meta event, called after GuiceBundle initialization (most of configuration done). Pure marker event, indicating Guicey work finished during the Dropwizard configuration phase.                                                                                                                                                                                                            | Last chance to modify Bootstrap
**Dropwizard run phase** |                                                                                                                                                                                                                                                                                                                                                                                       |
BeforeRun | Meta event, called before any Guicey actions just to indicate the first point where Environment, Configuration and introspected configuration are available                                                                                                                                                                                                                               | For example, used by `bundle.printConfigurationBindings()` to print configuration bindings before injector start (helps with missed-binding debugging) |
BundlesStarted^**?**^ | Called after bundles start (run method call). Not called if no bundles were used at all. Called only if bindings analysis is not disabled.                                                                                                                                                                                                                                            | Logging
ModulesAnalyzed | Called after Guice module analysis and repackaging. Reveals all detected extensions and removed binding info.                                                                                                                                                                                                                                                                       | Logging, analysis validation logic
ExtensionsResolved | Called to indicate all enabled extensions (manual, from classpath scan and modules). Always called to indicate configuration state.                                                                                                                                                                                                                                                   | Logging or remembering list of all enabled extensions (classes only)
InjectorCreation | Called just before Guice injector creation. Provides all configured modules (main and override) and all disabled modules. Always called.                                                                                                                                                                                                                                              | Logging. Note that it is useless to modify module instance here, because they were already processed.
**Guice injector created** |                                                                                                                                                                                                                                                                                                                                                                                       |
ExtensionsInstalledBy | Called when an installer installs all related extensions (for each installer) and only for installers that actually performed installations (extensions list never empty). Note: jersey extensions are processed later.                                                                                                                                                                      | Logging of installed extensions. Extension instance could be obtained from injector and post processed.
ExtensionsInstalled^**?**^ | Called after all installers install related extensions. Not called when there are no installed extensions (nothing registered or all disabled)                                                                                                                                                                                                                                                  | Logging or extensions post processing
ApplicationRun | Meta event, called when the Guice injector is started, extensions are installed (except jersey extensions because neither jersey nor jetty would be started yet) and all guice singletons initialized. At this point injection to registered commands is performed (this may be important if custom command run application instead of "server"). Point is just before `Application.run` method. | Ideal point for jersey and jetty listeners installation (with shortcut methods in event).
**Jersey initialization** |                                                                                                                                                                                                                                                                                                                                                                                       |
ApplicationStarting | Meta event, called after application run method, but before web server startup (called for lightweight tests)                                                                                                                                                                                                                                                                         | It is used for starting lightweight jersey context (in stub rest).
JerseyConfiguration | Jersey context starting. Both jersey and jetty are starting.                                                                                                                                                                                                                                                                                                                          | First point where jersey's `InjectionManager` (and `ServiceLocator`) become available
JerseyExtensionsInstalledBy | Called when jersey installer installed all related extensions (for each installer) and only for installers actually performed installations (extensions list never empty)                                                                                                                                                                                                             | Logging of installed extensions. Extension instance could be obtained from injector/locator and post processed.
JerseyExtensionsInstalled^**?**^ | Called after all jersey installers install related extensions. Not called when no installed extensions (nothing registered or all disabled). At this point HK2 is not completely started yet (and so extensions)                                                                                                                                                                      | Logging or extensions post processing
ApplicationStarted | Meta event, called after complete Dropwizard startup. This event also will be fired in Guicey lightweight tests                                                                                                                                                                                                                                                                       | May be used as assured "started" point (after all initializations). For example, for reporting.
ApplicationShutdown | Meta event, called on server shutdown start. This event also will be fired in Guicey lightweight tests                                                                                                                                                                                                                                                                                | May be used for shutdown logic.
ApplicationStoppedEvent | Meta event, called after application shutdown. This event also will be fired in Guicey lightweight tests                                                                                                                                                                                                                                                                              | May be used in rare cases to clean up file system resources after application stop.

^?^ - event may not be called

## Listeners

Event listener registration:

```java
GuiceBundle.builder()
    .listen(new MyListener(), new MyOtherListener())
    ...
    .build()
```

!!! note
    Listeners can also be registered in a Guicey bundle, but they will not receive all events:

    * `>= BundlesInitialized` for listeners registered in initialization method
    * `>= BundlesStarted` for listeners registered in run method

An event listener can implement the generic event interface `GuiceyLifecycleListener` and use the
enum to differentiate required events:

```java
public class MyListener implements GuiceyLifecycleListener {
    
    public void onEvent(GuiceyLifecycleEvent event) {
        switch (event.getType()) {
            case InjectorCreation:
                InjectorCreationEvent e = (InjectorCreationEvent) event;
                ...
        }
    }
}
```

Or use the `GuiceyLifecycleAdapter` adapter and override only required methods:

```java
public class MyListener extends GuiceyLifecycleAdapter {

    @Override
    protected void injectorCreation(final InjectorCreationEvent event) {
           ...
    }        
}
```

!!! tip
    In `ApplicationStarted` and `ApplicationShutdown` events, a lightweight Guicey test
    environment may be differentiated from real server startup with the `.isJettyStarted()` method.

### De-duplication

Event listeners also support de-duplication to prevent unnecessary duplicate usage
(for example, two bundles may register one listener because they are not always used together).
But it is **not the same mechanism** as configuration items de-duplication.

Simply, listeners are registered in a `LinkedHashSet`, and so they can control de-duplication
with proper `equals` and `hashCode` implementations.

Many reports use this feature (because all of them are based on listeners). For example,
[diagnostic report](diagnostic/configuration-report.md) uses the following implementations:

```java
@Override
public boolean equals(final Object obj) {
    // allow only one instance with the same title
    return obj instanceof ConfigurationDiagnostic
            && reportTitle.equals(((ConfigurationDiagnostic) obj).reportTitle);
}

@Override
public int hashCode() {
    return reportTitle.hashCode();
}
```

And with it, `.printDiagnosticInfo()` can be called multiple times and still only one report
will be actually printed.

### Events hierarchy

All event classes inherit from base event classes. These base event classes extend each other:
as lifecycle phases progress, more objects become available. So you can access any available object
from the event instance.

Base event | Description
-----------|-------------
GuiceyLifecycleEvent | The lowest event type. Provides access to event type and options.
ConfigurationPhaseEvent | Initialization phase event. Provides access to Bootstrap.
RunPhaseEvent | Dropwizard run phase. Provides access to Configuration, ConfigurationTree, Environment. Shortcut for the configuration bindings report renderer
InjectorPhaseEvent | Guice injector created. Provides the injector and GuiceyConfigurationInfo (Guicey configuration). Shortcuts for configuration report renderers
JerseyPhaseEvent | Jersey starting. Jersey's `InjectionManager` available.
