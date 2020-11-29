# Guicey lifecycle events

Guicey broadcast lifecycle events in all major points. Each event 
provides access to all available state at this point.

Events could be used for configuration analysis, reporting or to add some special 
post processing for configuration items (e.g. post process modules before injector creation). 

!!! important
    Event listeners could not modify configuration itself
    (can't add new extensions, installers, bundles or disable anything).   

## Events

All events are listed in `GuiceyLifecycle` enum (in execution order). 


Event |  Description  | Possible usage
------|---------------|---------------
**Dropwizard initialization phase** |   |  
ConfigurationHooksProcessed^**?**^ | Called after all registered hooks processing. Not called when no hooks used. | Only for info
DropwizardBundlesInitialized^**?**^ | Called after dropwizard bundles initialization (for dropwizard bundles registered through guicey api). Not called if no bundles were registered. | Logging, bundle instances modification (to affect run method) 
BundlesFromLookupResolved^**?**^ | Called after resolution bundles through lookup mechanism. Not called if no bundles found. | Logging or post processing of found bundles.
BundlesResolved | Called with all known top-level bundles (transitive bundles are not yet known). Always called to indicate configuration state.  | Could be used to modify top-level bundle instances
BundlesInitialized^**?**^ | Called after all bundles initialization (including transitive, so list of bundles could be bigger). Not called when no bundles registered. | Logging, post processing
CommandsResolved^**?**^ | Called if commands search is enabled and at least one command found | Logging
InstallersResolved | Called when all configured (and resolved by classpath scan) installers initialized | Potentially could be used to configure installer instances
ManualExtensionsValidated^**?**^ | Called when all manually registered extension classes are recognized by installers (validated). But only extensions, known to be enabled at that time are actually validated (this way it is possible to exclude extensions for non existing installers). Called only if at least one manual extension registered. | Logging, assertions
ClasspathExtensionsResolved^**?**^ | Called when classes from classpath scan analyzed and all extensions detected (if extension is also registered manually it would be also counted as from classpath scan). Called only if classpath scan is enabled and at least one extension detected. | Logging, assertions         
Initialized |  Meta event, called after GuiceBundle initialization (most of configuration done). Pure marker event, indicating guicey work finished under dropwizard configuration phase. | Last chance to modify Bootstrap  
**Dropwizard run phase** |   |
BeforeRun | Meta event, called before any guicey actions just to indicate first point where Environment, Configuration and introspected configuration are available | For example, used by `bundle.printConfigurationBindings()` to print configuration bindings before injector start (help with missed bindings debug) |
BundlesStarted^**?**^ | Called after bundles start (run method call). Not called if no bundles were used at all. Called only if bindings analysis is not disabled. | Logging
ModulesAnalyzed | Called after guice modules analysis and repackaging. Reveals all detected extensions and removed bindings info. | Logging, analysis validation logic
ExtensionsResolved | Called to indicate all enabled extensions (manual, from classpath scan and modules). Always called to indicate configuration state. | Logging or remembering list of all enabled extensions (classes only)
InjectorCreation | Called just before guice injector creation. Provides all configured modules (main and override) and all disabled modules. Always called. | Logging. Note that it is useless to modify module instance here, because they were already processed.
**Guice injector created** |   | 
ExtensionsInstalledBy | Called when installer installed all related extensions (for each installer) and only for installers actually performed installations (extensions list never empty). Note: jersey extensions are processed later. | Logging of installed extensions. Extension instance could be obtained from injector and post processed.
ExtensionsInstalled^**?**^ | Called after all installers install related extensions. Not called when no installed extensions (nothing registered or all disabled) | Logging or extensions post processing
ApplicationRun | Meta event, called when guice injector started, extensions installed (except jersey extensions because neither jersey nor jetty is't start yet) and all guice singletons initialized. At this point injection to registered commands is performed (this may be important if custom command run application instead of "server"). Point is just before `Application.run` method. | Ideal point for jersey and jetty listeners installation (with shortcut methods in event).
**Jersey initialization** |   |   
JerseyConfiguration | Jersey context starting. Both jersey and jetty are starting. | First point where jersey's `InjectionManager` (and `ServiceLocator`) become available
JerseyExtensionsInstalledBy | Called when jersey installer installed all related extensions (for each installer) and only for installers actually performed installations (extensions list never empty) | Logging of installed extensions. Extension instance could be obtained from injector/locator and post processed.
JerseyExtensionsInstalled^**?**^ | Called after all jersey installers install related extensions. Not called when no installed extensions (nothing registered or all disabled). At this point HK2 is not completely started yet (and so extensions) | Logging or extensions post processing
ApplicationStarted | Meta event, called after complete dropwizard startup. This event also will be fired in guicey lightweight tests | May be used as assured "started" point (after all initializations). For example, for reporting.
ApplicationShutdown | Meta event, called on server shutdown start. This event also will be fired in guicey lightweight tests | May be used for shutdown logic.
ApplicationStoppedEvent | Meta event, called after application shutdown. This event also will be fired in guicey lightweight tests | May be used in rare cases to cleanup fs resources after application stop.

^?^ - event may not be called

## Listeners

Events listener registration: 

```java
GuiceBundle.builder()
    .listen(new MyListener(), new MyOtherListener())
    ...
    .build()
```

!!! note
    Listeners could be also registered in guicey bundle, but they will not receive all events:
    
    * `>= BundlesInitialized` for listeners registered in initialization method 
    * `>= BundlesStarted` for listeners registered in run method  

Event listener could implement generic event interface `GuiceyLifecycleListener` and use
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

Or use `GuiceyLifecycleAdapter` adapter and override only required methods:

```java
public class MyListener extends GuiceyLifecycleAdapter {

    @Override
    protected void injectorCreation(final InjectorCreationEvent event) {
           ...
    }        
}
``` 

!!! tip
    In `ApplicationStarted` and `ApplicationShutdown` events lightweight guicey test
    environment may be differentiated from real server startup with `.isJettyStarted()` method.

### De-duplication

Event listeners are also support de-duplication to prevent unnecessary  duplicates usage
(for example, two bundles may register one listener because they are not always used together).
But it is **not the same mechanism** as configuration items de-duplication.

Simply listeners are registered in the `LinkedHashSet` and so listeners could control de-duplication
with a proper `equals` and `hashCode` implementations

Many reports use this feature (because all of them are based on listeners). For example,
[diagnostic report](diagnostic/configuration-report.md) use the following implementations:

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

All event classes inherit from some base event classes. Base event classes are extending each other:
as lifecycle phases go, more objects become available. So you can access any available (at this point) object
from event instance. 

Base event | Description 
-----------|-------------
GuiceyLifecycleEvent | The lowest event type. Provides access to event type and options.
ConfigurationPhaseEvent | Initialization phase event. Provides access to Bootstrap.  
RunPhaseEvent | Dropwizard run phase. Provides access to Configuration, ConfigurationTree, Environment. Shortcut for configuration bindings report renderer
InjectorPhaseEvent | Guice injector created. Available injector and GuiceyCofigurationInfo (guicey configuration). Shortcuts for configuration reports renderer 
JerseyPhaseEvent | Jersey starting. Jersey's `InjectionManager` available.
 