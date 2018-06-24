# Guicey lifecycle events

Guicey broadcast lifecycle events in all major points. Each event 
provides access to all available state at this point.

Events could be used for configuration analysis or to add some special post processing
(e.g. post process modules before injector creation). But events could not modify configuration itself
(can't add new extensions, installers, bundles or disable anything).   

## Debug

Use `bundle.printLifecyclePhases()` to see lifecycle events in logs.
Could be very helpful during problems investigation. Also, it shows startup timer to easily see where most startup time is spent.

Example output:

```

                                                                         ─────────────────────────
__[ 00:00:00.008 ]____________________________________________________/  1 hooks processed  \____



                                                                         ────────────────────
__[ 00:00:00.104 ]____________________________________________________/  2 commands installed  \____

INFO  [2018-06-15 04:09:56,978] io.dropwizard.server.DefaultServerFactory: Registering jersey handler with root path prefix: /
INFO  [2018-06-15 04:09:56,981] io.dropwizard.server.DefaultServerFactory: Registering admin handler with root path prefix: /


                                                                         ────────────────────────────────────
__[ 00:00:00.881 ]____________________________________________________/  Configured from 3 (-1) GuiceyBundles  \____



                                                                         ──────────────────────────────────────
__[ 00:00:00.886 ]____________________________________________________/  Staring guice with 3/0 (-1) modules...  \____



                                                                         ─────────────────────────────
__[ 00:00:00.980 ]____________________________________________________/  8 (-1) installers initialized  \____



                                                                         ────────────────────────
__[ 00:00:01.008 ]____________________________________________________/  13 (-1) extensions found  \____

...
```

Note that `(-1)` in some events means disabled items and actual displayed count did not count disabled items.
Use detailed output (`bundle.printLifecyclePhasesDetailed()`) to see more details. 

Example detailed output (for one event):

```
                                                                         ────────────────────────────────────
__[ 00:00:00.893 ]____________________________________________________/  Configured from 3 (-1) GuiceyBundles  \____

	bundles = 
		DiagnosticBundle             (r.v.d.g.m.c.debug)        
		GuiceRestrictedConfigBundle  (r.v.d.g.support.util)     
		CoreInstallersBundle         (r.v.d.g.m.installer)      

	disabled = 
		HK2DebugBundle               (r.v.d.g.m.j.debug)      
``` 

!!! important
    Lifecycle logs are printed to system out instead of logger because logger is not yet initialized on first events. 
    Anyway, these logs intended to be used for problems resolution and so console only output should not be a problem. 

## Registration

Events listener could be registered only through main bundle: 

```java
GuiceBundle.builder()
    .listen(new MyListener(), new MyOtherListener())
    ...
    .build()
```  

All events are listed in `GuiceyLifecycle` enum in execution order. 

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

### Context modification

Event listeners are not allowed to modify configuration, only observe it and, if required, 
post process instances (modules, bundles etc).

But, if listener needs to register any additional extension it can implement `GuiceyConfigrator` interface
to be automatically registered as configuration hook:

```java
public class MyDebuggingListener extends GuiceyLifecycleAdapter 
                                  implements GuiceyConfigurationHook {
    
    @Override
    public void configure(GuiceBundle.Builder builder) {
        builder
            .extensions(DebugExtension.class)
            .bundles(new DebugBundle())
            ...        
    }

    @Override
    protected void injectorCreation(final InjectorCreationEvent event) {
           ...
    }        
}
```  

### Event structure

All events inherit from base event classes. Event classes are extending each other:
as initialisation phases go more objects become available. So you can access any available (at this point) object
from event instance. 

Base event | Description 
-----------|-------------
GuiceyLifecycleEvent | The lowest event type. Initialization dropwizard phase. Provides access to event type and options. 
RunPhaseEvent | Dropwizard run phase. Available Bootstrap, Configuration, ConfigurationTree, Environment. Shortcut for configuration bindings report renderer
InjectorPhaseEvent | Guice injector created. Available injector and GuiceyCofigurationInfo (guicey configuration). Shortcuts for configuration reports renderer 
HK2PhaseEvent | Jersey starting. ServiceLocator available.


## Events

Note that some events may not been called - (?).

Event |  Description  | Possible usage
------|---------------|---------------
 **Dropwizard initialization phase** |   |  
HooksProcessed | Called after all registered GuiceyConfigurationHook processing | Only for info
Initialization |  Called after GuiceBundle initialization. If commands search is enabled then all found commands will be provided in event. | Convenient moment to apply registrations into dropwizard Bootstrap object.
**Dropwizard run phase** |   |
BeforeRun | Meta event, called before guicey configuration just to indicate first point where Environment, Configuration and introspected configuration are available | For example, used by `bundle.printConfigurationBindings()` to print configuration bindings before injector start (help with missed bindings debug)
BundlesFromDwResolved(?) | Called if configuration from dw bundles enabled and at least one bundle recognized  (note: some of these bundles could be actually disabled and not used further) | Logging or post processing of recognized bundles.
BundlesFromLookupResolved(?) | Called if at least one bundle recognized using bundles lookup (note: some of these bundles could be disabled and not used further) | Logging or post processing of found bundles.
BundlesResolved | Called to indicate all top-level bundles (registered manually, from lookup and recognized dropwizard bundles). Called even if no bundles registered to indicate configuration state. |  Logging or post processing of top-level bundles.
BundlesProcessed(?) | Called after bundles processing. Note that bundles could register other bundles and so resulted list of installed bundles could be bigger (than in resolution event). Called only when at lest one bundle registered and not disabled. | Logging of all used bundles or storing bundles list for modules (for example).
InjectorCreation | Called just before guice injector creation. Provides all configured modules (main and override) and all disabled modules. Always called, event if no modules registered. | Logging or post processing modules (can't add or remove modules, but can manipulate module instance)
InstallersResolved | Called when installers resolved (from classpath scan, if enabled) and initialized. Injector is under creation at that moment. | Logging or post processing of installers (e.g. set list of modules or bundles to some installer)
ExtensionsResolved | Called when all extensions detected (from classpath scan, if enabled). Injector is under creation at that moment. | Logging or remembering list of all enabled extensions (classes only)
**Guice injector created** |   | 
ExtensionsInstalledBy | Called when installer installed all related extensions (for each installer) and only for installers actually performed installations (extensions list never empty). Note: jersey extensions are processed later. | Logging of installed extensions. Extension instance could be obtained from injector and post processed.
ExtensionsInstalled(?) | Called after all installers install related extensions. Not called when no installed extensions (nothing registered or all disabled) | Logging or extensions post processing
ApplicationRun | Called when guice injector started, extensions installed (except jersey extensions because neither jersey nor jetty is't start yet) and all guice singletons initialized. Point is just before `Application.run` method. | Ideal point for jersey and jetty listeners installation (with shortcut methods in event).
**Jersey initialization (HK2)** |   |   
HK2Configuration | HK2 context starting. Both jersey and jetty are starting. | First point where ServiceLocator become available
HK2ExtensionsInstalledBy | Called when jersey installer installed all related extensions (for each installer) and only for installers actually performed installations (extensions list never empty) | Logging of installed extensions. Extension instance could be obtained from injector/locator and post processed.
HK2ExtensionsInstalled(?) | Called after all jersey installers install related extensions. Not called when no installed extensions (nothing registered or all disabled). At this point HK2 is not completely started yet (and so extensions) | Logging or extensions post processing 