# Configuration

Guicey main dropwizard bundle must be registered:

```java
@Override
public void initialize(Bootstrap<Configuration> bootstrap) {
    bootstrap.addBundle(GuiceBundle.builder() 
            // <configuration methods>           
            .build());
}
```

Guicey could be configured through:

* [Main (dropwizard) bundle](#main-bundle)
* [Bundles](#guicey-bundle)
* [Hooks](#hooks)

## Main bundle

!!! tip
    Bundle builder contains shortcuts for all guicey features, so required function may be found only by looking at available methods (and reading javadoc).       

### Configuration items

`#!java .enableAutoConfig(String... basePackages)`
:   Enable classpath scan for automatic extensions registration, custom installers search and commands search (if enabled) 
   
`#!java .modules(Module... modules)`
:   Guice modules registration

    !!! note ""
        Here guice modules are created in initialization phase, when `Configuration` and `Environment` are
        not yet available. If they are required for module either use "Aware" interfaces or register
        module in `GuiceyBundle`'s run method (under run phase).    

`#!java .modulesOverride(Module... modules)`
:   Overriding registered guice modules bindings (using guice `Modules.override()` api)
    
    !!! warning ""
        Extensions are not recognized in overriding modules (intentionally)!

`#!java .installers(Class<? extends FeatureInstaller>... installers)`
:   Guicey extension installers registration. Required if you have custom installers or
    to specify installers after disabling all default installers: `#!java .noDefaultInstallers()`
    
    !!! note ""
        Custom installers are registered automatically when classpath scan is enabled.  

`#!java .extensions(Class<?>... extensionClasses)`  
:   Manual extensions registration. May be used together with classpath scan and binding extensions

`#!java .bundles(GuiceyBundle... bundles)` 
:   Guicey bundles registration. 

`#!java .bundleLookup(GuiceyBundleLookup bundleLookup)`
:   Custom lookup mechanism for guicey bundles. By default lookup by system property and
    ServiceLoader are enabled. To disable all lookups use: `#!java .disableBundleLookup()`

`#!java .dropwizardBundles(ConfiguredBundle... bundles)`
:   Shortcut for dropwizard bundles registration. This way guicey could apply disable and de-duplication rules
    to registered bundles (and, also, registered bundles appear in reports)

`#!java .searchCommands()`
:   Search and register custom dropwizard commands. Requires enabled classpath scan   

### Disable items

Registered configuration items could be disabled. This is mostly useful for tests where
entire application parts could be disabled and replaced (e.g. with mocks) this way.
Could be also useful to "hack" third party items.

`#!java .disableInstallers(Class<? extends FeatureInstaller>... installers)`  
`#!java .disableExtensions(Class<?>... extensions)`  
`#!java .disableModules(Class<? extends Module>... modules)`
:   !!! warning ""
        Affects transitive guice modules
  
`#!java .disableBundles(Class<? extends GuiceyBundle>... bundles)`   
`#!java .disableDropwizardBundles(Class<? extends ConfiguredBundle>... bundles)`
:   !!! warning ""
        Affects only dropwizard bundles registered through guicey api and their transitive bundles
          
`#!java .disable(Predicate<ItemInfo>... predicates)`  
:   Custom disable predicate useful to disable groups of items (by some sign)

### Items de-duplication

Guiey detects instances of the same type (bundles, modules). By default, two instances 
considered as duplicates if they are equal, so duplicates could be controlled with proper 
equals method implementation. When it's not possible, custom de-duplication implementation could be used.

!!! tip
    Special base classes are available with correct equals implementations:
    `UniqueModule` (for guice modules) and `UniqueGuiceyBundle` (for bundles).

`#!java .duplicateConfigDetector(DuplicateConfigDetector detector)`
:   !!! note ""
        Special implementation provided to replicate legacy guicey behaviour "one instance per class":
        `#!java duplicateConfigDetector(new LegacyModeDuplicatesDetector())`
              
`#!java .uniqueItems(Class<?>... configurationItems)`
:   Register special de-duplication implementation which will allow only one instance of provided types. 


### Options

Guicey generic options mechanism may be used for guicey (or other 3rd party bundles) fine-tuning.

!!! note ""
    Guicey option enums: `GuiceyOptions` and `InstallersOptions` 

`#!java .option(K option, Object value)`
:   Set option value (override default)
    
`#!java .options(Map<Enum, Object> options)`
:   Set multiple options at once (e.g. map system properties as option values)

### Injector

`#!java .injectorFactory(InjectorFactory injectorFactory)`
:   Use custom injector factory implementation. May be useful for tests or for integration
    of 3rd paty library (like governator)
      
`#!java .build(Stage stage)`
:   Build bundle with custom guice stage (by default, `Production`) 
  
`#!java .build()`
:   Build bundle with deafault guice stage

### Lifecycle

`#!java .listen(GuiceyLifecycleListener... listeners)`
:   Listen for guicey lifecycle events
  
`#!java .noGuiceFilter()`
:   Disable `GucieFilter` registration.
    !!! danger ""
        This will remove guice request and session scopes and also
        it would become impossible to use `ServletModule`s
          
`#!java .strictScopeControl()`
:   Explicitly detect when gucie bean is instantiated with HK2 and vice versa.
    !!! note ""
        Bean target container is defined with `@JerseyManaged` and `@GuiceManaged` annotations
        or default (either guice or hk2 used as default (for jersey extensions))
  
`#!java .useHK2ForJerseyExtensions()`
:   Use HK2 by default for jersey extensions (change default). With this `@GuiceManaged` annotation
    may be used to override defual for bean.
    !!! danger ""
        Beans managed by HK2 can't use guice AOP, so AOP-based features will not work with such beans      

!!! danger
    In the next version guicey will get rid of HK2 and so all HK2 related options will be removed
    (only guice will be used). Also, `.noGuiceFilter()` will be removed because request scope will be required.  

### Diagnostic tools

Guicey provide many bundled console reports to help with problems diagnostic (or to simply clarify how application works)
during development, like:

```java 
.printDiagnosticInfo()
```  

See [diagnostic section](diagnostic/diagnostic-tools.md) for a full list of available reports.

### Hooks-related

`#!java .hookAlias(String name, Class<? extends GuiceyConfigurationHook> hook)`
:   Hook alias registration for simplified usage (various diagnostic tools quick enabling with a system property)
   
`#!java .withSharedState(Consumer<SharedConfigurationState> stateAction)` 
:   This method is mainly useful for hooks, becase it's the only way to access application
    shared state from [hook](#hooks).

## Guicey bundle

`GuiceyBundle`s are like dropwizard bundles, but with greater abilities. Supposed to be used instead of 
dropwizard bundles. Bundles are registered either directly (in main bundle or other guicey bundle)
 or resolved by bundles lookup.

### Initialization

```java
public class MyBundle implements GuiceyBundle {
       
    @Override
    public void initialize(GuiceyBootstrap bootstrap) {
        ...
    }      
}
```   

Bundle initialization share many methods in common with [main guice bundle](#main-bundle):

`#!java .modules(Module... modules)`  
`#!java .modulesOverride(Module... modules)`   
`#!java .installers(Class<? extends FeatureInstaller>... installers)`   
`#!java .extensions(Class<?>... extensionClasses)`     
`#!java .bundles(GuiceyBundle... bundles)`      
`#!java .dropwizardBundles(ConfiguredBundle... bundles)`
  
`#!java .disableInstallers(Class<? extends FeatureInstaller>... installers)`  
`#!java .disableExtensions(Class<?>... extensions)`  
`#!java .disableModules(Class<? extends Module>... modules)`

!!! note ""
    No disable for bundles because at this moment some bundles were already executed and so 
    real state could be inconsistent with configuration (and highly depend on processing order). 

`#!java .listen(GuiceyLifecycleListener... listeners)`
:   !!! warning ""
        Listener registered in bundle will "hear" only events starting with `GuiceyLifecycle#BundlesInitialized` 

Shortcuts:

`#!java .bootstrap()`   
`#!java .application()`   

Option value access:

`#!java .option(T option)`  
:   !!! warning ""
        Bundle can't declare option value because it would make options state not predictable
        (highly dependent on initialization order)  

Shared state access:

`#!java .shareState(Class<?> key, Object value)`
:   Declare shared state (primary module scenario)

`#!java .sharedState(Class<?> key, Supplier<T> defaultValue)`
:   Get or init shared state (equal bundles scenario)

`#!java .sharedStateOrFail(Class<?> key, String message, Object... args)`
:   Shortcut to get shared state or immediately fail if not declared    

### Run

```java
public class MyBundle implements GuiceyBundle {
       
    @Override
    public void run(GuiceyEnvironment environment) {
        ...
    }      
}
```

Almost everything is configured under initialization phase and so on run phase
bundle allows only modules registration  

Shortcuts:

`#!java .configuration()`  
`#!java .environment()`   
`#!java .application()`  
`#!java .register(Object... items)`
:   Shortcut for `#!java environment().jersey().register(Object)`

`#!java .register(Class<?>... items)`
:   Shortcut for `#!java environment().jersey().register(Class)`

`#!java .manage(Managed managed)`
:   Shortcut for `#!java environment().lifecycle().manage()`

`#!java .listen(ServerLifecycleListener listener)`
:   Shortcut for `#!java environment().lifecycle().addServerLifecycleListener()`

`#!java .listen(LifeCycle.Listener listener)`
:   Shortcut for `#!java environment().lifecycle().addLifeCycleListener()`

Extended configuration access:

`#!java .configuration(String yamlPath)`  
`#!java .configuration(Class<T> type)`  
`#!java .configurations(Class<T> type)`  
`#!java .configurationTree()`   

Modules registration:

`#!java .modules(Module... modules)`  
:   !!! note ""
        Only here bundles may be created directly with configuration values
        
`#!java .modulesOverride(Module... modules)`

Disables:

`#!java .disableExtensions(final Class<?>... extensions)`  
`#!java .disableModules(Class<? extends Module>... modules)`  
 
Option value access:

`#!java .option(T option)`

Shared state:

`#!java .sharedStateOrFail(Class<?> key, String message, Object... args)`  
`#!java .sharedState(Class<?> key)`

Guicey listeners:

`#!java .listen(GuiceyLifecycleListener... listeners)`
:   !!! warning ""
        Listener registered in run phase will "hear" only events starting 
        with `GuiceyLifecycle#BundlesStarted` 

`#!java .onGuiceyStartup(GuiceyStartupListener listener)`
:   Shortcut for manual configuration under run phase with available injector

`#!java .onApplicationStartup(ApplicationStartupListener listener)`
:   Shortcut for manual actions after complete application start (jetty started) 

    !!! note ""
        It is also called after guicey initialization in lightweight guicey tests

## Hooks

Guicey hooks are registered statically **before** main guice bundle registration:

```java
public class MyHook implements GuiceyConfigurationHook {

    @Override
    public void configure(final GuiceBundle.Builder builder) {
        builder.printDiagnosticInfo();
    }
}    

// static registration
new MyHook().register() 
```        

On execution hook receives *the same* builder as used in main `GuiceBundle`. 
So hooks could configure *everything*.

Hooks are intended to be used in tests and to implement a pluggable diagnostic tools 
activated with system property `-Dguicey.hooks=...` (as an example, see guicey `DiagnosticHook`).