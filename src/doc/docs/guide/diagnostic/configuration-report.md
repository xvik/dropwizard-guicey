# Configuration report

!!! note
    During startup guicey records startup metrics and remembers all details of configuration process. 
    All this information is available through GuiceyConfigurationInfo bean:
    
    ```java
    @Inject GuiceyConfigurationInfo info;
    ``` 

Configuration diagnostic report is the most commonly used report allowing you to see guicey startup and 
configuration details (the last is especially important for de-duplication logic diagnostic).

```java
GuiceBundle.builder() 
    ...
    .printDiagnosticInfo()
    .build());
```   

Report intended to answer:

* [How guicey spent time](#timings)
* [What options used](#used-options)
* [What was configured](#configuration-summary)
* [From where configuration items come from](#configuration-tree)


Example report:

```    
INFO  [2019-10-11 04:25:47,022] ru.vyarus.dropwizard.guice.debug.ConfigurationDiagnostic: Diagnostic report

---------------------------------------------------------------------------[STARTUP STATS]

    GUICEY started in 431.2 ms (150.2 ms config / 279.4 ms run / 1.594 ms jersey)
    │   
    ├── [0.70%] CLASSPATH scanned in 3.088 ms
    │   ├── scanned 5 classes
    │   └── recognized 4 classes (80% of scanned)
    │   
    ├── [24%] BUNDLES processed in 105.8 ms
    │   ├── 1 resolved in 12.22 ms
    │   ├── 7 initialized in 23.75 ms
    │   └── 1 dropwizard bundles initialized in 69.58 ms
    │   
    ├── [2.6%] COMMANDS processed in 11.34 ms
    │   └── registered 2 commands
    │   
    ├── [9.3%] MODULES processed in 40.14 ms
    │   ├── 7 modules autowired
    │   ├── 8 elements found in 5 user modules in 36.53 ms
    │   └── 1 extensions detected from 3 acceptable bindings
    │   
    ├── [7.4%] INSTALLERS processed in 32.60 ms
    │   ├── registered 12 installers
    │   └── 4 extensions recognized from 10 classes in 11.63 ms
    │   
    ├── [47%] INJECTOR created in 204.2 ms
    │   ├── Module execution: 124 ms
    │   ├── Interceptors creation: 2 ms
    │   ├── TypeListeners & ProvisionListener creation: 2 ms
    │   ├── Scopes creation: 1 ms
    │   ├── Binding creation: 20 ms
    │   ├── Module annotated method scanners creation: 1 ms
    │   ├── Binding initialization: 29 ms
    │   ├── Collecting injection requests: 2 ms
    │   ├── Static validation: 3 ms
    │   ├── Instance member validation: 3 ms
    │   ├── Provider verification: 1 ms
    │   ├── Static member injection: 7 ms
    │   ├── Instance injection: 3 ms
    │   └── Preloading singletons: 4 ms
    │   
    ├── [0.70%] EXTENSIONS installed in 3.594 ms
    │   ├── 4 extensions installed
    │   └── declared as: 2 manual, 1 scan, 1 binding
    │   
    ├── [0.23%] JERSEY bridged in 1.594 ms
    │   ├── using 2 jersey installers
    │   └── 3 jersey extensions installed in 501.8 μs
    │   
    └── [7.4%] remaining 32 ms


---------------------------------------------------------------------------[OPTIONS]

    Guicey                    (r.v.dropwizard.guice.GuiceyOptions)
        ScanPackages                   = [ru.vyarus.dropwizard.guice.diagnostic.support.features] *CUSTOM
        SearchCommands                 = true                           *CUSTOM
        UseCoreInstallers              = true                           
        BindConfigurationByPath        = true                           
        TrackDropwizardBundles         = true                           
        AnalyzeGuiceModules            = true                           
        InjectorStage                  = PRODUCTION                     
        GuiceFilterRegistration        = [REQUEST]                      
        UseHkBridge                    = false                          


    Installers                (r.v.d.g.m.i.InstallersOptions)
        JerseyExtensionsManagedByGuice = true                           
        ForceSingletonForJerseyExtensions = true                           


---------------------------------------------------------------------------[CONFIGURATION]

    COMMANDS = 
        Cli                          (r.v.d.g.d.s.features)     *SCAN
        EnvCommand                   (r.v.d.g.d.s.features)     *SCAN, GUICE_ENABLED


    BUNDLES = 
        FooDwBundle                  (r.v.d.g.d.s.dwbundle)     *DW
        Foo2Bundle                   (r.v.d.g.d.s.bundle)       
            FooBundleRelative2Bundle     (r.v.d.g.d.s.bundle)       
        HK2DebugBundle               (r.v.d.g.m.j.debug)        *HOOK, REG(1/2)
        GuiceRestrictedConfigBundle  (r.v.d.g.support.util)     *HOOK
        CoreInstallersBundle         (r.v.d.g.m.installer)      
            WebInstallersBundle          (r.v.d.g.m.installer)      
        LookupBundle                 (r.v.d.g.d.s.bundle)       *LOOKUP


    INSTALLERS and EXTENSIONS in processing order = 
        jerseyfeature        (r.v.d.g.m.i.f.j.JerseyFeatureInstaller) *REG(1/2)
            HK2DebugFeature              (r.v.d.g.m.j.d.service)    
        resource             (r.v.d.g.m.i.f.j.ResourceInstaller)    
            FooBundleResource            (r.v.d.g.d.s.bundle)       *REG(1/3)
            FooResource                  (r.v.d.g.d.s.features)     *SCAN
            ModuleFeature                (r.v.d.g.d.s.m.ModuleWithExtensions) *BINDING


    GUICE MODULES = 
        FooModule                    (r.v.d.g.d.s.features)     *REG(2/2)
        ModuleWithExtensions         (r.v.d.g.d.s.module)       
        FooBundleModule              (r.v.d.g.d.s.bundle)       
        HK2DebugModule               (r.v.d.g.m.j.d.HK2DebugBundle) 
        GRestrictModule              (r.v.d.g.s.u.GuiceRestrictedConfigBundle) 
        OverridingModule             (r.v.d.g.d.s.module)       *OVERRIDE
        GuiceBootstrapModule         (r.v.d.guice.module)       


---------------------------------------------------------------------------[CONFIGURATION TREE]

    APPLICATION
    ├── extension  FooBundleResource            (r.v.d.g.d.s.bundle)       
    ├── extension  -FooBundleResource           (r.v.d.g.d.s.bundle)       *DUPLICATE
    ├── module     FooModule                    (r.v.d.g.d.s.features)     
    ├── module     ModuleWithExtensions         (r.v.d.g.d.s.module)       
    ├── module     GuiceBootstrapModule         (r.v.d.guice.module)       
    ├── -disable   LifeCycleInstaller           (r.v.d.g.m.i.feature)      
    ├── FooDwBundle                  (r.v.d.g.d.s.dwbundle)     *DW
    │   
    ├── Foo2Bundle                   (r.v.d.g.d.s.bundle)       
    │   ├── extension  -FooBundleResource           (r.v.d.g.d.s.bundle)       *DUPLICATE
    │   ├── module     FooBundleModule              (r.v.d.g.d.s.bundle)       
    │   ├── -disable   ManagedInstaller             (r.v.d.g.m.i.feature)      
    │   │   
    │   └── FooBundleRelative2Bundle     (r.v.d.g.d.s.bundle)       
    │       └── module     FooModule#2                  (r.v.d.g.d.s.features)     
    │   
    ├── HK2DebugBundle               (r.v.d.g.m.j.debug)        
    │   ├── installer  JerseyFeatureInstaller       (r.v.d.g.m.i.f.jersey)     
    │   ├── extension  HK2DebugFeature              (r.v.d.g.m.j.d.service)    
    │   └── module     HK2DebugModule               (r.v.d.g.m.j.d.HK2DebugBundle) 
    │   
    ├── CoreInstallersBundle         (r.v.d.g.m.installer)      
    │   ├── installer  -JerseyFeatureInstaller      (r.v.d.g.m.i.f.jersey)     *DUPLICATE
    │   ├── installer  ResourceInstaller            (r.v.d.g.m.i.f.jersey)     
    │   └── WebInstallersBundle          (r.v.d.g.m.installer)      
    │   
    ├── BUNDLES LOOKUP
    │   │   
    │   └── LookupBundle                 (r.v.d.g.d.s.bundle)       
    │       └── module     OverridingModule             (r.v.d.g.d.s.module)       
    │   
    ├── CLASSPATH SCAN
    │   └── extension  FooResource                  (r.v.d.g.d.s.features)     
    │   
    ├── GUICE BINDINGS
    │   │   
    │   └── ModuleWithExtensions         (r.v.d.g.d.s.module)       
    │       └── extension  ModuleFeature                (r.v.d.g.d.s.m.ModuleWithExtensions) 
    │   
    └── HOOKS
        ├── -HK2DebugBundle              (r.v.d.g.m.j.debug)        *DUPLICATE
        │   
        └── GuiceRestrictedConfigBundle  (r.v.d.g.support.util)     
            └── module     GRestrictModule              (r.v.d.g.s.u.GuiceRestrictedConfigBundle)     
```

## Timings

!!! note
    There are also another ["time-report"](lifecycle-report.md) available which shows entire
    application startup timings.

### Startup timings

```
    GUICEY started in 431.2 ms (150.2 ms config / 279.4 ms run / 1.594 ms jersey)
```

Guicey time (`431.2 ms`) is measured as `GuiceBundle` methods plus part of jersey configuration time (jersey started after bundle).
It also show time spent on each application starting phase: `150.2 ms` configuration (initialization), `279.4 ms` run and `1.594 ms` during jersey startup.

All items below represent guicey time detalization. Tree childs always detail time of direct parent.                                                                     

!!! tip
    Application startup during development may be improved with VM options:
    ```
    -XX:TieredStopAtLevel=1 -noverify
    ```
    It does not show a big difference on test sample, but it's already a notable change (~20% faster):
    ```
    GUICEY started in 304.0 ms (88.18 ms config / 214.6 ms run / 1.130 ms jersey)
    ``` 

### Classpath scan

```
    ├── [0.70%] CLASSPATH scanned in 3.088 ms
    │   ├── scanned 5 classes
    │   └── recognized 4 classes (80% of scanned)
```

Classpath scan performed just once. Represents only time spent resolving all classes in configured packages. 
Guicey will later use this resolved classes to search commands (if enabled), installers and extensions. 

`scanned 5 classes` 
:   means that 5 classed were found (overall) in configured packages.
   
`recognized 4 classes` 
:   show effectiveness of classpath scanning (how many classes were actually used as installer, extension or command).

!!! note 
    Classpath scan time will be obviously bigger for real applications (with larger classes count). 
    But most of this time spent on class loading (becauase guicey loads all classes during scan and not 
    just parse class structure). If you use all these classes then they will be loaded 
    in any case. If you disable classpath scan to save time then this time will just move to other places
    (where classes are used). 

### Bundles

```
    ├── [24%] BUNDLES processed in 105.8 ms
    │   ├── 1 resolved in 12.22 ms
    │   ├── 7 initialized in 23.75 ms
    │   └── 1 dropwizard bundles initialized in 69.58 ms
```

Bundles time includes bundles lookup time (if not .disableBundleLookup()), bundles execution (both init and run)
and dropwizard bundles (known by guicey) init time (run time is not tracked because it appears after 
`GuiceBundle.run`).

`1 resolved` 
:   indicated bundles resolved with guicey bundle lookup.
  
`7 initialized` 
:   overall processed bundles (all registered bundles, including transitives). Time cover both init and run.

`1 dropwizard bundles initialized`
:   dropwizard bundles (registered through guicey api!) initialization time.
    !!! note ""
        Dropwizard bundles always include ~50ms overhead of Bootstrap object proxying,
        required for transitive bundles tracking.

### Commands

```
    ├── [2.6%] COMMANDS processed in 11.34 ms
    │   └── registered 2 commands
```

Commands time includes time spent on commands search (in classes from already performed classpath scan), 
and calling `.injectMemebers` on found environment commands (last part is always performed, but it's very fast so most likely commands section will not appear 
if `.searchCommands()` is not enabled)

!!! note
    Most of commands time will be command objects instantiation (reflection).

### Modules

```
    ├── [9.3%] MODULES processed in 40.14 ms
    │   ├── 7 modules autowired
    │   ├── 8 elements found in 5 user modules in 36.53 ms
    │   └── 1 extensions detected from 3 acceptable bindings
```             

`7 modules autowired`
:    `Aware*` interfaces processing for registered modules (user modules + overrides + 1 guicey module) 

`8 elements found in 5 user modules`
:   Guicey performs modules introspection before injector creation (with guice SPI) and shows all found elements. 
    Only user modules are introspected (except override and guicey module).

`1 extensions detected from 3 acceptable bindings`
:   Shows detected extensions from bindings. Note that overall modules elements count include
    aop, listeners etc. Also, bindings are searched only in direct class bindings, so
    acceptable elements count will almost always be lower then overall elements count.

### Installers

```
    ├── [7.4%] INSTALLERS processed in 32.60 ms
    │   ├── registered 12 installers
    │   └── 4 extensions recognized from 10 classes in 11.63 ms
```   

Shows installers initialization and processing time.

`registered 12 installers`
:   Overall count of used installers (core and custom, without disables).

`4 extensions recognized from 10 classes`
:   Overall recognition time (potential extension classes introspection time).
    Includes class from classpath scan, all manual extension classes and checked (acceptable) bindings.

### Injector

```
    ├── [47%] INJECTOR created in 204.2 ms
    │   ├── Module execution: 124 ms
    │   ├── Interceptors creation: 2 ms
    │   ├── TypeListeners & ProvisionListener creation: 2 ms
    │   ├── Scopes creation: 1 ms
    │   ├── Binding creation: 20 ms
    │   ├── Module annotated method scanners creation: 1 ms
    │   ├── Binding initialization: 29 ms
    │   ├── Collecting injection requests: 2 ms
    │   ├── Static validation: 3 ms
    │   ├── Instance member validation: 3 ms
    │   ├── Provider verification: 1 ms
    │   ├── Static member injection: 7 ms
    │   ├── Instance injection: 3 ms
    │   └── Preloading singletons: 4 ms
```

Pure injector creation time. Sub logs are intercepted guice internal logs (from
`com.google.inject.internal.util`). Only lines with non 0 time are shown.

### Extensions

```
    ├── [0.70%] EXTENSIONS installed in 3.594 ms
    │   ├── 4 extensions installed
    │   └── declared as: 2 manual, 1 scan, 1 binding
``` 

Extensions are installed just after injector creation. Time represent installers
installation logic time. 

`4 extensions installed`
:   All used (installed, not disabled) extensions

`declared as: 2 manual, 1 scan, 1 binding`
:   Extensions sources: manual registration, classpath scan, bindings in guice modules.
    One extension could appear multiple times in counters (if it was detected in multiple sources)

### Jersey

```
    ├── [0.23%] JERSEY bridged in 1.594 ms
    │   ├── using 2 jersey installers
    │   └── 3 jersey extensions installed in 501.8 μs
```

Jersey starts after dropwizard bundles processing and so after `GuiceBundle` execution. 
Guicey register required HK2 bindings (and some HK2 beans in guice) and executes jersey 
installers (installers implementing `JerseyInstaller`) to process jersey specific features. 
For example, all resources and jersey extensions installed here (because requires HK2 specific bindings).

`using 2 jersey installers`
:   All jersey feature installers (not disabled)    

`3 jersey extensions installed`
:   All jersey extensions (not disabled)

Note that extensions installation time is so tiny (`501.8 μs`) just because empty resources (without methods) were used. 
In real application installation time will be bigger.

### Remaining

```
    └── [7.4%] remaining 32 ms
```

Represent not explicitly tracked time, spent by guicey for other small operations. Shown on tree to indicate that all major parts were shown.

## Used options

Shows all guicey options set or requested (by application logic). If you use your own options here they will also be printed.

``` 
    Guicey                    (r.v.dropwizard.guice.GuiceyOptions)
        ScanPackages                   = [ru.vyarus.dropwizard.guice.diagnostic.support.features] *CUSTOM
        SearchCommands                 = true                           *CUSTOM
        UseCoreInstallers              = true                           
        BindConfigurationByPath        = true                           
        TrackDropwizardBundles         = true                           
        AnalyzeGuiceModules            = true                           
        InjectorStage                  = PRODUCTION                     
        GuiceFilterRegistration        = [REQUEST]                      
        UseHkBridge                    = false                          


    Installers                (r.v.d.g.m.i.InstallersOptions)
        JerseyExtensionsManagedByGuice = true                           
        ForceSingletonForJerseyExtensions = true 
```

Used markers:

* `CUSTOM` - option value set by user
* `NOT_USED` - option was set by user but never used

!!! note
    `NOT_USED` marker just indicates that option is "not yet" used. Options may be consumed lazilly by application logic, so
    it is possible that its not used at reporting time. There is no such cases with guicey options, 
    but may be with your custom options (it all depends on usage scenario).

## Configuration summary

Section intended to show all configuration summary (to quickly see what was configured).

This and the next sections used condensed package notion:

```
CoreInstallersBundle         (r.v.d.g.m.installer)    
```

Assumed that all classes in application will be uniquely identifiable by name so package info shown just to be able to 
understand exact class location. Logback shrinker used.

Report indicates duplicate registrations and items of the same type registrations: `REG(1/3)`,
where first number is accepted instances count and last number is overall registrations count.
For extensions it's always `1/N`, but for instances (modules, bundles) it indicates 
de-duplication mechanism work. 

### Commands

```
    COMMANDS = 
        Cli                          (r.v.d.g.d.s.features)     *SCAN
        EnvCommand                   (r.v.d.g.d.s.features)     *SCAN, GUICE_ENABLED
```

Shows commands resolved with classpath scan (enabled with `.searchCommands()`).

The following markers used:

* `SCAN` - item from classpath scan (always)
* `GUICE_ENABLED` - marks environment command, which could contain guice injections (other commands simply doesn't trigger application run and so injector never starts)

### Bundles

```
    BUNDLES = 
        FooDwBundle                  (r.v.d.g.d.s.dwbundle)     *DW
        Foo2Bundle                   (r.v.d.g.d.s.bundle)       
            FooBundleRelative2Bundle     (r.v.d.g.d.s.bundle)       
        HK2DebugBundle               (r.v.d.g.m.j.debug)        *HOOK, REG(1/2)
        GuiceRestrictedConfigBundle  (r.v.d.g.support.util)     *HOOK
        CoreInstallersBundle         (r.v.d.g.m.installer)      
            WebInstallersBundle          (r.v.d.g.m.installer)      
        LookupBundle                 (r.v.d.g.d.s.bundle)       *LOOKUP
```

All registered bundles are shown as a tree (to indicate transitive bundles).

Markers used:

* `LOOKUP` - bundle resolved with bundle lookup mechanism
* `DW` - dropwizard bundle
* `HOOK` - registered by [configuration hook](../configuration.md#guicey-configuration-hooks)

### Installers and extensions

```
    INSTALLERS and EXTENSIONS in processing order = 
        jerseyfeature        (r.v.d.g.m.i.f.j.JerseyFeatureInstaller) *REG(1/2)
            HK2DebugFeature              (r.v.d.g.m.j.d.service)    
        resource             (r.v.d.g.m.i.f.j.ResourceInstaller)    
            FooBundleResource            (r.v.d.g.d.s.bundle)       *REG(1/3)
            FooResource                  (r.v.d.g.d.s.features)     *SCAN
            ModuleFeature                (r.v.d.g.d.s.m.ModuleWithExtensions) *BINDING
```

Shows used installers (only installers which install extensions) and installed extensions. 

Both installers and extensions are shown in the processing order (sorted according to `@Order` annotations).

Markers used:

* `SCAN` - item from classpath scan (even if extension or installer were registered manually also to indicate item presence in classpath scan)
* `LAZY` - extensions annotated with `@LazyBinding`
* `JERSEY` - extension annotated with `@JerseyManaged`
* `HOOK` - registered by [configuration hook](../configuration.md#guicey-configuration-hooks)
* `BINDING` - extension recognized from guice binding

### Modules

```
    GUICE MODULES = 
        FooModule                    (r.v.d.g.d.s.features)     *REG(2/2)
        ModuleWithExtensions         (r.v.d.g.d.s.module)       
        FooBundleModule              (r.v.d.g.d.s.bundle)       
        HK2DebugModule               (r.v.d.g.m.j.d.HK2DebugBundle) 
        GRestrictModule              (r.v.d.g.s.u.GuiceRestrictedConfigBundle) 
        OverridingModule             (r.v.d.g.d.s.module)       *OVERRIDE
        GuiceBootstrapModule         (r.v.d.guice.module) 
```

All registered guice modules: user modules + override modules + guicey `GuiceBootstrapModule`.
Overriding modules are marked with `OVERRIDE`.

## Configuration tree

Configuration tree is useful to understand from where configuration items come from.

!!! note
    Configuration tree shows both guicey bundles and dropwizard bundle trees (for dropwizard
    bundles, registered through guicey api). Dropwizard bundles are identified with marker `DW`.

There are several sections in tree:

* `APPLICATION` - for everything registered in `GuiceBundle`
* `BUNDLES LOOKUP` - for bundles resolved with lookup mechanism
* `CLASSPATH SCAN` - for items resolved by classpath scan
* `GUICE BINDINGS` - for extensions resolved from guice module bindings
* `HOOKS` - for registered hooks

Markers:

* `DW` - dropwizard bundle
* `DISABLED` - item manually disabled
* `DUPLICATE` - item considered as duplicate for already registered item

### Disables

Manual disable is identified (to quickly understand who disabled item) as:

```
    ├── -disable   LifeCycleInstaller           (r.v.d.g.m.i.feature)  
```

And disabled items would be also identified:

```
    ├── installer  -LifeCycleInstaller           (r.v.d.g.m.i.feature)         *DISABLED  
```

Disabled bundles are shown without context (to identify disable):

```
    -HK2DebugBundle              (r.v.d.g.m.j.debug)        *DISABLED
```

### De-duplication

Duplicate registration or instance de-duplications are identified as:

```
    ├── extension  -FooBundleResource           (r.v.d.g.d.s.bundle)       *DUPLICATE
```

If extension is registered more then once in the same context, it would be identified:

```
    ├── extension  FooBundleResource           (r.v.d.g.d.s.bundle) 
    ├── extension  -FooBundleResource           (r.v.d.g.d.s.bundle)       *DUPLICATE
```    

If more then one instance (in this context) is considered to be duplicate then number will appear:

```
    ├── extension  -FooBundleResource           (r.v.d.g.d.s.bundle)       *DUPLICATE(3)
```    

When multiple instances of the same type is registered (form modules or bundles), they would be numbered
in order of registration (to differentiate):

``` 
    APPLICATION
    ├── module     FooModule                    (r.v.d.g.d.s.features)
    ... 
    │   └── FooBundleRelative2Bundle     (r.v.d.g.d.s.bundle)       
    │       └── module     FooModule#2                  (r.v.d.g.d.s.features) 
```

In order to better track duplicates, items, recognized as duplicates, will be shown with
registered instance they considered to be duplicate to. For example, for above sample,
if another `FooModule` registered and it would be considered as duplicate to second instance,
it would be printed as:

```
└── module     -FooModule#2                 (r.v.d.g.d.s.features)  *DUPLICATE
```

### Guice bindings

Extensions detected from guice bindings are shown as a sub tree:

```
    ├── GUICE BINDINGS
    │   │   
    │   └── ModuleWithExtensions         (r.v.d.g.d.s.module)       
    │       └── extension  ModuleFeature                (r.v.d.g.d.s.m.ModuleWithExtensions) 
    
```    

!!! note
    Extensions are shown relative to top-most registered modules!

    For example, if `ModuleWithExtensions` internally install module `ExtensionModule` which 
    actually contains extension bingind, then still `ExtensionModule` will not be shown and 
    extension will remain under `ModuleWithExtensions` (on report).
    
    This was done to not confuse users: report shows guicey configration and user must clearly
    see configuration source.    

Bindings are not shown under main configuration tree (where modules are registered) because
guicey know only module class, but actually moduliple module instances could be registered
and so it is impossible to known what module instance extension is related to.

!!! tip
    Detailed guice modules tree could be seen on [guice report](guice-report.md)
    
## Customization

`.printDiagnosticInfo()` shortcut register report with default settings. If you need customized
report then register report listener directly. For example, [installers report](installers-report.md) is a 
configured configuration report:

```java
.listen(ConfigurationDiagnostic.builder("Available installers report")
                    .printConfiguration(new DiagnosticConfig()
                            .printInstallers()
                            .printNotUsedInstallers()
                            .printInstallerInterfaceMarkers())
                    .printContextTree(new ContextTreeConfig()
                            .hideCommands()
                            .hideDuplicateRegistrations()
                            .hideEmptyBundles()
                            .hideExtensions()
                            .hideModules())
                    .build());
```

Report rendering logic may also be used directly as all sub-reports provide separate renderer object
implementing `ReportRenderer`. Renderers not bound to guice context and assume direct instantiation. 

For examples of direct renderers usage see [events](../events.md) implementation:

* `InjectorPhaseEvent.ReportRenderer`         