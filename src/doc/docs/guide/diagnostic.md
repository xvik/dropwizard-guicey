# Diagnostic info

During startup guicey records startup metrics and remembers all details of configuration process. 
All this information is available through [GuiceyConfigurationInfo](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/GuiceyConfigurationInfo.java) bean in guice context.

## Diagnostic service 

Default diagnostic info rendering is provided to assist configuration problems resolution and better guicey internals understanding.

The simplest way to enable diagnostic reporting is using bundle `.printDiagnosticInfo()` option. 
This registers [DiagnosticBundle](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/context/debug/DiagnosticBundle.java) with default reporting configuration. 
Bundle could be registered directly in order to customize output.

When `.printDiagnosticInfo()` enabled, the following kind of logs will be printed after server startup:

```    
INFO  [2016-08-01 21:22:50,898] ru.vyarus.dropwizard.guice.module.context.debug.report.DiagnosticReporter: Startup stats = 

    GUICEY started in 453.3 ms
    │   
    ├── [0,88%] CLASSPATH scanned in 4.282 ms
    │   ├── scanned 5 classes
    │   └── recognized 4 classes (80% of scanned)
    │   
    ├── [4,2%] COMMANDS processed in 19.10 ms
    │   └── registered 2 commands
    │   
    ├── [6,4%] BUNDLES processed in 29.72 ms
    │   ├── 2 resolved in 8.149 ms
    │   └── 6 processed
    │   
    ├── [86%] INJECTOR created in 390.3 ms
    │   ├── installers prepared in 13.79 ms
    │   │   
    │   ├── extensions recognized in 9.259 ms
    │   │   ├── using 11 installers
    │   │   └── from 7 classes
    │   │   
    │   └── 3 extensions installed in 4.188 ms
    │   
    ├── [1,3%] HK bridged in 6.583 ms
    │   ├── using 2 jersey installers
    │   └── 2 jersey extensions installed in 660.9 μs
    │   
    └── [1,1%] remaining 5 ms
    
INFO  [2016-08-01 21:22:50,901] ru.vyarus.dropwizard.guice.module.context.debug.report.DiagnosticReporter: Options = 

    Guicey                    (r.v.dropwizard.guice.GuiceyOptions)
        ScanPackages                   = [ru.vyarus.dropwizard.guice.diagnostic.support.features] *CUSTOM
        SearchCommands                 = true                           *CUSTOM
        UseCoreInstallers              = true                           
        ConfigureFromDropwizardBundles = false                          
        InjectorStage                  = PRODUCTION                     
        GuiceFilterRegistration        = [REQUEST]                 
  
INFO  [2016-08-01 21:22:50,901] ru.vyarus.dropwizard.guice.module.context.debug.report.DiagnosticReporter: Configuration diagnostic info = 

    COMMANDS = 
        Cli                          (r.v.d.g.d.s.features)     *SCAN
        EnvCommand                   (r.v.d.g.d.s.features)     *SCAN, GUICE_ENABLED


    BUNDLES = 
        CoreInstallersBundle         (r.v.d.g.m.installer)      
        Foo2Bundle                   (r.v.d.g.d.s.bundle)       
            FooBundleRelative2Bundle     (r.v.d.g.d.s.bundle)       
        HK2DebugBundle               (r.v.d.g.m.j.debug)        *LOOKUP, REG(2)
        DiagnosticBundle             (r.v.d.g.m.c.debug)        
        GuiceRestrictedConfigBundle  (r.v.d.g.support.util)     *LOOKUP


    INSTALLERS and EXTENSIONS in processing order = 
        jerseyfeature        (r.v.d.g.m.i.f.j.JerseyFeatureInstaller) *REG(2)
            HK2DebugFeature              (r.v.d.g.m.j.d.service)    
        resource             (r.v.d.g.m.i.f.j.ResourceInstaller)    
            FooBundleResource            (r.v.d.g.d.s.bundle)       *REG(3)
            FooResource                  (r.v.d.g.d.s.features)     *SCAN


    GUICE MODULES = 
        FooModule                    (r.v.d.g.d.s.features)     *REG(2)
        FooBundleModule              (r.v.d.g.d.s.bundle)       
        HK2DebugModule               (r.v.d.g.m.j.d.HK2DebugBundle) 
        DiagnosticModule             (r.v.d.g.m.c.d.DiagnosticBundle) 
        GRestrictModule              (r.v.d.g.s.u.GuiceRestrictedConfigBundle) 
        GuiceSupportModule           (r.v.d.guice.module)       
        
INFO  [2016-08-01 21:22:50,909] ru.vyarus.dropwizard.guice.module.context.debug.report.DiagnosticReporter: Configuration context tree = 

    APPLICATION
    ├── extension  FooBundleResource            (r.v.d.g.d.s.bundle)       
    ├── module     FooModule                    (r.v.d.g.d.s.features)     
    ├── module     GuiceSupportModule           (r.v.d.guice.module)       
    ├── -disable   LifeCycleInstaller           (r.v.d.g.m.i.feature)      
    │   
    ├── Foo2Bundle                   (r.v.d.g.d.s.bundle)       
    │   ├── extension  FooBundleResource            (r.v.d.g.d.s.bundle)       *IGNORED
    │   ├── module     FooBundleModule              (r.v.d.g.d.s.bundle)       
    │   ├── -disable   ManagedInstaller             (r.v.d.g.m.i.feature)      
    │   │   
    │   └── FooBundleRelative2Bundle     (r.v.d.g.d.s.bundle)       
    │       └── module     FooModule                    (r.v.d.g.d.s.features)     *IGNORED
    │   
    ├── HK2DebugBundle               (r.v.d.g.m.j.debug)        
    │   ├── installer  JerseyFeatureInstaller       (r.v.d.g.m.i.f.jersey)     
    │   ├── extension  HK2DebugFeature              (r.v.d.g.m.j.d.service)    
    │   └── module     HK2DebugModule               (r.v.d.g.m.j.d.HK2DebugBundle) 
    │   
    ├── DiagnosticBundle             (r.v.d.g.m.c.debug)        
    │   └── module     DiagnosticModule             (r.v.d.g.m.c.d.DiagnosticBundle) 
    │   
    ├── CoreInstallersBundle         (r.v.d.g.m.installer)      
    │   ├── installer  JerseyFeatureInstaller       (r.v.d.g.m.i.f.jersey)     *IGNORED
    │   └── installer  ResourceInstaller            (r.v.d.g.m.i.f.jersey)     
    │   
    ├── BUNDLES LOOKUP
    │   ├── HK2DebugBundle               (r.v.d.g.m.j.debug)        *IGNORED
    │   │   
    │   └── GuiceRestrictedConfigBundle  (r.v.d.g.support.util)     
    │       └── module     GRestrictModule              (r.v.d.g.s.u.GuiceRestrictedConfigBundle) 
    │   
    └── CLASSPATH SCAN
        └── extension  FooResource                  (r.v.d.g.d.s.features)  
```

Default reporting contains 4 sections:

* How guicey spent time
* What options used
* What was configured
* From where configuration items come from

## Timings

### Startup timings

```
    GUICEY started in 453.3 ms
```

Overall guicey time measured: GuiceBundle methods plus part of Hk configuration time (hk started after bundle).
All items below represent guicey time detalization. Items always detail time of direct parent.

Most of this time actually spent on class loading. For example, report above represent [test](https://github.com/xvik/dropwizard-guicey/blob/master/src/test/groovy/ru/vyarus/dropwizard/guice/config/debug/DiagnosticBundleTest.groovy) direct execution. 
But when this test executed as part of suit time become  `GUICEY started in 52.95 ms` because most classes were pre-loaded by other tests.

### Classpath scan

```
    ├── [0,88%] CLASSPATH scanned in 4.282 ms
    │   ├── scanned 5 classes
    │   └── recognized 4 classes (80% of scanned)
```

Classpath scan performed just once. Represents only time spent resolving all classes in configured packages. Guicey will later use this resolved classes to search commands (if enabled), installers and extensions. 

`scanned 5 classes` means that 5 classed were found (overall) in configured packages. `recognized 4 classes` Show effectiveness of classpath scanning (how many classes were actually used as installer, extension or command).

NOTE: classpath scan time will be obviously bigger for larger classes count. But most of this time are actually class loading time. If you use all these classes then they will be loaded in any case. If you disable classpath scan to save time then this time move to other some place (for example, to injector creation). 

### Commands

```
    ├── [4,2%] COMMANDS processed in 19.10 ms
    │   └── registered 2 commands
```

Commands time includes time spent on commands search (in classes from classpath scan; if enabled .searchCommands()) and calling .injectMemebers on configured environment commands (last part is always performed, but it's very fast so most likely commands section will not appear if .searchCommands() is not enabled)

### Bundles

```
    ├── [6,4%] BUNDLES processed in 29.72 ms
    │   ├── 2 resolved in 8.149 ms
    │   └── 6 processed
```

Bundles time includes bundles lookup time (if not .disableBundleLookup()), dropwizard bunles lookup (if .configureFromDropwizardBundles()) and bundles execution.

`2 resolved in 8.149 ms` indicated bundles resolved with guicey bundle lookup or from dropwizard bundles.
`6 processed` - overall processed bundles (all registered bundles, including just resolved).

### Injector

```
    ├── [86%] INJECTOR created in 390.3 ms
    │   ├── installers prepared in 13.79 ms
    │   │   
    │   ├── extensions recognized in 9.259 ms
    │   │   ├── using 11 installers
    │   │   └── from 7 classes
    │   │   
    │   └── 3 extensions installed in 4.188 ms
```

All installers and extensions operations (except jersey related features) performed inside of guice module and so included into overall injector creation time.

`installers prepared in 13.79 ms` include installers search in classpath (if scan enabled), instantiation and preparing for usage (remove duplicates, sort).

`extensions recognized in 9.259 ms` - all manually configured extensions and all classes from classpath scan (`from 7 classes`) are recognized by all registered installers (`using 11 installers`) using installer match method. Recognized installers bound to guice context (or custom action performed for binding installers).

`3 extensions installed in 4.188 ms` - all recognized extensions are installed with installers install methods.

!!! note
    Most time of injector creation is internal guice logic. You can enable guice logs to see more details (see below)

### HK

```
    ├── [1,3%] HK bridged in 6.583 ms
    │   ├── using 2 jersey installers
    │   └── 2 jersey extensions installed in 660.9 μs
```

Jersey starts after dropwizard bundles processing and so after GuiceBundle execution. This time is tracked as (overall) guicey time. Here guicey register required HK bindings and (some hk beans in guice) and executes jersey installers (installers implement JerseyInstaller) to process jersey specific features. For example, all resources and jersey extensions installed here (because requires hk specific bindings).

Note that installation time (`2 jersey extensions installed in 660.9 μs`) is so tiny just because empty resources (without methods) were used. In real application installation time will be bigger.

### Remaining

```
    └── [1,1%] remaining 5 ms
```

Represent not explicitly tracked time, spent by guicey for other small operations. Shown on tree to indicate that all major parts were shown.

## Used options

Shows all set or requested (by application logic) options. If you use your own options here they will also be printed.

``` 
    Guicey                    (r.v.dropwizard.guice.GuiceyOptions)
        ScanPackages                   = [ru.vyarus.dropwizard.guice.diagnostic.support.features] *CUSTOM
        SearchCommands                 = true                           *CUSTOM
        UseCoreInstallers              = true                           
        ConfigureFromDropwizardBundles = false                          
        InjectorStage                  = PRODUCTION                     
        GuiceFilterRegistration        = [REQUEST]     
        
```

Used markers:
* CUSTOM - option value set by user
* NOT_USED - option was set by user but never used

Not used marker just indicated that option is "not yet" used. Options may be consumed lazilly by application logic, so
it is possible that its not used at reporting time. There is no such cases with guicey options, but may be with your custom options (it all depends on usage scenario).

## Configuration diagnostic info

Section intended to compactly show all configuration (to quickly see what was configured).

This and the next sections used condensed package notion:

```
CoreInstallersBundle         (r.v.d.g.m.installer)    
```

Assumed that all classes in application will be uniquely identifiable by name and package info shown just to be able to 
understand exact class location. Logback shrinker used.

Report also indicates duplicate registrations by REG(N) marker, where N - amount of installations 
(ignored installations will be visible in configuration tree). Counted:

* item registration in different places (e.g. different bundles)
* duplicate registrations in simgle place (e.g. `.extensions(MyExt.class, MyExt.class)`)

### General

All configuration items (commands, modules, installers, extension, bundles) are identified by class. Duplicate entities are not allowed and simply ignored.

For example, if extension registered manually and by classpath scan then it will be registered once, but internally guicey will remember both configuration sources.

In contrast to other items, bundles and modules are registered by instance, but still uniqueness is checked by type: only first instance registered and other instances considered as duplicate.

### Commands

```
    COMMANDS = 
        Cli                          (r.v.d.g.d.s.features)     *SCAN
        EnvCommand                   (r.v.d.g.d.s.features)     *SCAN,GUICE_ENABLED
```

Shows commands resolved with classpath scan (enabled with .searchCommands()).

The following markers used:

* SCAN - item from classpath scan (always)
* GUICE_ENABLED - marks environment command, which could contain guice injections (other commands simply doesn't trigger application run and so injector never starts)

### Bundles

```
    BUNDLES = 
        CoreInstallersBundle         (r.v.d.g.m.installer)      
        Foo2Bundle                   (r.v.d.g.d.s.bundle)       
            FooBundleRelative2Bundle     (r.v.d.g.d.s.bundle)       
        HK2DebugBundle               (r.v.d.g.m.j.debug)        *LOOKUP, REG(2)
        DiagnosticBundle             (r.v.d.g.m.c.debug)        
        GuiceRestrictedConfigBundle  (r.v.d.g.support.util)     *LOOKUP
```

All registered bundles are shown as a tree (to indicate transitive bundles).

The following markers used:

* LOOKUP - bundle resolved with bundle lookup mechanism
* DW - bundle recognized from registered dropwizard bundle

### Installers and extensions

```
    INSTALLERS and EXTENSIONS in processing order = 
        jerseyfeature        (r.v.d.g.m.i.f.j.JerseyFeatureInstaller) *REG(2)
            HK2DebugFeature              (r.v.d.g.m.j.d.service)    
        resource             (r.v.d.g.m.i.f.j.ResourceInstaller)    
            FooBundleResource            (r.v.d.g.d.s.bundle)       *REG(3)
            FooResource                  (r.v.d.g.d.s.features)     *SCAN
```

Shows used installers (only installers which install extensions) and installed extensions. 

Both installers and extensions are shown in the processing order (sorted according to @Order annotations).

The following markers used:

* SCAN - item from classpath scan (even if extension or installer were registered manually also to indicate item presence in classpath scan)
* LAZY - extensions annotated with @LazyBinding
* HK - extension annotated with @HK2Managed

### Modules

```
    GUICE MODULES = 
        FooModule                    (r.v.d.g.d.s.features)     *REG(2)
        FooBundleModule              (r.v.d.g.d.s.bundle)       
        HK2DebugModule               (r.v.d.g.m.j.d.HK2DebugBundle) 
        DiagnosticModule             (r.v.d.g.m.c.d.DiagnosticBundle) 
        GRestrictModule              (r.v.d.g.s.u.GuiceRestrictedConfigBundle) 
        GuiceSupportModule           (r.v.d.guice.module)  
```

All registered guice modules.

## Configuration context tree

Configuration tree is useful to understand from where configuration items come from.

Installer disables are shown like:

```
    ├── -disable   LifeCycleInstaller           (r.v.d.g.m.i.feature) 
```

Duplicate registrations (ignored by guicey) are shown like:

```
    │   ├── extension  FooBundleResource            (r.v.d.g.d.s.bundle)       *IGNORED
```

If number in configuration report (e.g.REG(3)) doesn't match registration appearances, then item registered
multiple times in one of this places.

Note that CoreInstallersBundle are always below all other bundles. This is because it always registered last 
(to be able to disable it's registration). It doesn't affect anything other than reporting (because bundles order does 
not change anything except this tree).

## Re-using

Diagnostic info rendering may be used for custom rendering (showing in web page or some other staff).
Rendering is performed with 3 beans, available for injection (when bundle registered):

* [StatsRenderer](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/context/debug/report/stat/StatsRenderer.java)
* [OptionsRenderer](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/context/debug/report/option/OptionsRenderer.java)
* [DiagnosticRenderer](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/context/debug/report/diagnostic/DiagnosticRenderer.java)
* [ContextTreeRenderer](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/context/debug/report/tree/ContextTreeRenderer.java) 

## Installers mode

There is a special option in GuiceBundle `.printAvailableInstallers()` for printing only installers information. 
It also use DiagnosticBundle, but with different configuration. 

!!! warning 
    Both options can't be used together (but it should never be required as they serve different purposes).

Example output:

```
INFO  [2016-08-22 00:49:33,557] ru.vyarus.dropwizard.guice.module.context.debug.report.DiagnosticReporter: Configuration diagnostic info = 

    INSTALLERS in processing order = 
        lifecycle            (r.v.d.g.m.i.f.LifeCycleInstaller)     
        managed              (r.v.d.g.m.i.feature.ManagedInstaller) 
        jerseyfeature        (r.v.d.g.m.i.f.j.JerseyFeatureInstaller) 
        jerseyprovider       (r.v.d.g.m.i.f.j.p.JerseyProviderInstaller) 
        resource             (r.v.d.g.m.i.f.j.ResourceInstaller)    
        eagersingleton       (r.v.d.g.m.i.f.e.EagerSingletonInstaller) 
        healthcheck          (r.v.d.g.m.i.f.h.HealthCheckInstaller) 
        task                 (r.v.d.g.m.i.feature.TaskInstaller)    
        plugin               (r.v.d.g.m.i.f.plugin.PluginInstaller) 
        webservlet           (r.v.d.g.m.i.f.w.WebServletInstaller)  
        webfilter            (r.v.d.g.m.i.f.web.WebFilterInstaller) 
        weblistener          (r.v.d.g.m.i.f.w.l.WebListenerInstaller) 

INFO  [2016-08-22 00:49:33,563] ru.vyarus.dropwizard.guice.module.context.debug.report.DiagnosticReporter: Configuration context tree = 

    APPLICATION
    │   
    ├── WebInstallersBundle          (r.v.d.g.m.installer)      
    │   ├── installer  WebFilterInstaller           (r.v.d.g.m.i.f.web)        
    │   ├── installer  WebServletInstaller          (r.v.d.g.m.i.f.web)        
    │   └── installer  WebListenerInstaller         (r.v.d.g.m.i.f.w.listener) 
    │   
    └── CoreInstallersBundle         (r.v.d.g.m.installer)      
        ├── installer  LifeCycleInstaller           (r.v.d.g.m.i.feature)      
        ├── installer  ManagedInstaller             (r.v.d.g.m.i.feature)      
        ├── installer  JerseyFeatureInstaller       (r.v.d.g.m.i.f.jersey)     
        ├── installer  JerseyProviderInstaller      (r.v.d.g.m.i.f.j.provider) 
        ├── installer  ResourceInstaller            (r.v.d.g.m.i.f.jersey)     
        ├── installer  EagerSingletonInstaller      (r.v.d.g.m.i.f.eager)      
        ├── installer  HealthCheckInstaller         (r.v.d.g.m.i.f.health)     
        ├── installer  TaskInstaller                (r.v.d.g.m.i.feature)      
        └── installer  PluginInstaller              (r.v.d.g.m.i.f.plugin)     
```

Comparing to complete diagnostic, it shows all installers (even not used). In diagnostic reporting not used installers are hidden, because usually it means they are not needed.

## Guice injector creation timings

You will see in guicey timings that almost all time spent creating guice injector. 
To see some guice internal timings enable guice debug logs:

```
logging:
  loggers:
    com.google.inject.internal.util: DEBUG
```

Logs will be something like this:

```
DEBUG [2016-08-03 21:09:45,963] com.google.inject.internal.util.Stopwatch: Module execution: 272ms
DEBUG [2016-08-03 21:09:45,963] com.google.inject.internal.util.Stopwatch: Interceptors creation: 1ms
DEBUG [2016-08-03 21:09:45,965] com.google.inject.internal.util.Stopwatch: TypeListeners & ProvisionListener creation: 2ms
DEBUG [2016-08-03 21:09:45,966] com.google.inject.internal.util.Stopwatch: Scopes creation: 1ms
DEBUG [2016-08-03 21:09:45,966] com.google.inject.internal.util.Stopwatch: Converters creation: 0ms
DEBUG [2016-08-03 21:09:45,992] com.google.inject.internal.util.Stopwatch: Binding creation: 26ms
DEBUG [2016-08-03 21:09:45,992] com.google.inject.internal.util.Stopwatch: Module annotated method scanners creation: 0ms
DEBUG [2016-08-03 21:09:45,993] com.google.inject.internal.util.Stopwatch: Private environment creation: 1ms
DEBUG [2016-08-03 21:09:45,993] com.google.inject.internal.util.Stopwatch: Injector construction: 0ms
DEBUG [2016-08-03 21:09:46,170] com.google.inject.internal.util.Stopwatch: Binding initialization: 177ms
DEBUG [2016-08-03 21:09:46,171] com.google.inject.internal.util.Stopwatch: Binding indexing: 1ms
DEBUG [2016-08-03 21:09:46,172] com.google.inject.internal.util.Stopwatch: Collecting injection requests: 1ms
DEBUG [2016-08-03 21:09:46,179] com.google.inject.internal.util.Stopwatch: Binding validation: 7ms
DEBUG [2016-08-03 21:09:46,183] com.google.inject.internal.util.Stopwatch: Static validation: 4ms
DEBUG [2016-08-03 21:09:46,191] com.google.inject.internal.util.Stopwatch: Instance member validation: 8ms
DEBUG [2016-08-03 21:09:46,192] com.google.inject.internal.util.Stopwatch: Provider verification: 1ms
DEBUG [2016-08-03 21:09:46,201] com.google.inject.internal.util.Stopwatch: Static member injection: 9ms
DEBUG [2016-08-03 21:09:46,204] com.google.inject.internal.util.Stopwatch: Instance injection: 3ms
DEBUG [2016-08-03 21:09:46,427] com.google.inject.internal.util.Stopwatch: Preloading singletons: 223ms
```

!!! note 
    'Preloading singletons' line will be logged **long after** other guice log messages, so search it at the end of your startup log.
