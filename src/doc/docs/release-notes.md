# Release Notes

Release contains many breaking changes due to:

* Dropwizard 2.0 (Jersey 2.26) breaking changes
* Change of guicey lifecycle

As dropwizard 2.0 already introduce many breaking changes (new jersey actually), so it was an ideal moment to fix 
all conceptual guicey mistakes. 

Please read [dropwizard 2.0 upgrade guide](https://github.com/dropwizard/dropwizard/blob/master/docs/source/manual/upgrade-notes/upgrade-notes-2_0_x.rst) first.
   
* [General changes](#general-changes)
* [New guicey lifecycle](#new-guicey-lifecycle)
* [Multiple bundles and modules of the same type support](#multiple-bundles-and-modules-of-the-same-type-support)
* [Dropwizard bundles direct support](#dropwizard-bundles-direct-support)
* [Configuration from guice bindings (jersey1-guice style)](#configuration-from-guice-bindings-jersey1-guice-style)
* [Multiple classloaders support](#multiple-classloaders-support)
* [Guicey listeners changes](#guicey-listeners-changes)
* [New shared configuration state](#new-shared-lifecycle-state)
* [Reporting changes](#reporting-changes)
* [Guicey hooks changes](#guicey-hooks-changes)
* [Guicey BOM includes guicey itself](#guicey-bom-includes-guicey-itself)
* [Java 11 compatibility](#java-11-compatibility)

**[Migration Guide](#migration-guide)**.

## General changes

Jersey 2.26 introduces an abstraction for injection layer in order to get rid of hk2 direct usage.
This allows complete hk2 avoidance in the future. Right now it means that all direct hk2 classes must be replaced
by jersey abstractions (see [migration matrix](#dropwizard-related) below)

Note that `org.glassfish.hk2.api.ServiceLocator` must be replaced with `org.glassfish.jersey.internal.inject.InjectionManager`.
The later may be used to obtain locator, but be aware that HK2 supposed to be completely dropped in the next guicey version.

`@HK2Managed` guicey annotation was renamed to `@JerseyManaged` to point on possible absence of HK2.

`GuiceBundle.builder()#useWebInstallers()` was removed because `WebInstallersBundle` is 
activated by default and so you don't need to activate it manually anymore. 

## New guicey lifecycle

Before guicey perform all it's operations under dropwizard run phase. This makes guicey and dropwizard
bundles interoperability hard. But guicey should not limit dropwizard abilities and so now
almost all configuration is performed under dropwizard's configuration phase.

New guicey lifecycle:

* Dropwizard bundles are resolved and initialized under configuration phase
* Classpath scan and extensions resolution appears under configuration phase
* Guice injector, as before, created under run phase.

`GuiceyBundle` interface changed from 

```java 
public interface GuiceyBundle {
    // called on run phase
    void initialize(GuiceyBootstrap bootstrap);
}
```                     

into 

```java
public interface GuiceyBundle {
    // called on configuration phase
    default void initialize(GuiceyBootstrap bootstrap) {}
    // called on run phase
    default void run(GuiceyEnvironment environment) {}
}
``` 

Default methods were used by analogy with updated dropwizard `ConfiguredBundle` to avoid need to always
implement both methods.                      

All registrations, except modules must be performed under configuration phase. On run phase only guice modules could be
registered because guice modules often require configuration values (without this it would always
require to use root `DropwizardAwareModule` to perform some modules registrations).

Extensions might be disabled in run phase: assumed that this could be required for
optional features, driven by configuration (not needed extensions could be disabled, based on 
configuration object values).

!!! note
    Dropwizard and guicey bundles are completely equivalent now, but guicey bundles provide more 
    configuration options (aware of guicey). So prefer using `GuiceyBundle` instead of dropwizard bundles. 
    Note that guicey bundles now provide shortcut methods for easy dropwizard bundles registration.

Before, extensions registration was performed under guice injector creation, but now it was moved 
just after injector creation in order to differentiate pure injector creation errors from
extension installation errors.  

!!! tip 
    Statistics report (`.printDiagnosticReports()`) will also print time by phase:
    ```
    GUICEY started in 441.1 ms (104.8 ms config / 336.2 ms run / 0 jersey)
    ```
    (configuration phase / run phase / extensions installation during jersey startup)

### Run phase shortcuts

GuiceyEnvironment almost not allow guicey items registrations (except modules), but 
provide many shortcuts:

* `register()` for `environment().jersey().register()`
* `manage()` for `environment().lifecycle().manage()` 
* `listen()` for `environment().lifecycle().addLifeCycleListener()` and `environment().lifecycle().addServerLifecycleListener()`
* `onGuiceyStartup()` - special callback called after guicey start to perform manual registrations in 
    dropwizard environment with provided injector
* `onApplicationStartup()` - special callback called after complete application startup (in guicey lightweight tests too)
    (with provided injector)     


## Multiple bundles and modules of the same type support

Before, guicey was always tracking configuration items by class and so when you register multiple bundles or modules
or the same type, only one was accepted: 

```java
.modules(new MyModule1(), new MyModule2())
```

But often it is required to register multiple modules/bundles with different parametrization:

```java
.modules(new ParametrizableModule("mod1"), new ParametrizableModule("mod2"))
```

Now guicey **allows multiple instances** of the same type. So, in examples above, both modules
would be registered and used.

!!! note 
    Multiple instances support is more logical in context of dropwizard, because dropwizard itself 
    allows registration of multiple bundles of the same type.
     

### Deduplication

In some cases, previous limitation was actually desired for deduplication: before, when two or more bundles use some
common bundle, they could safely register it and be sure that only one common bundle instance would be used.
Now, **both** common bundles would be registered.

To workaround such cases *deduplication mechanism* was introduced: instances of the same
type are considered duplicate if they are equal. So, in order to resolve "common bundle problem"
bundle must only properly implement equals method, like this:

```java
public class CommonBundle implements GuiceyBundle {
    ...
    
    @Override
    public boolean equals(final Object obj) {
        return obj != null && getClass().equals(obj.getClass());
    }
}
```                                    

!!! tip
    Guicey provide base classes for such cases:
    `UniqueGuiceyBundle` for unique bundles and `UniqueModule` for unique guice modules

But, it may be impossible to implement correct equals method for some 3rd party bundle or module used.
In this case unique objects may be marked with:

```java
GuiceBundle.builder()
    .uniqueItems(Some3rdPartyBundle.class, 
                 Some3rdPartyModule.class)
```

 
For complex cases (e.g. when only some kinds of objects must be unique), manual deduplication 
implementation may be registered:

```java
GuiceBundle.builder()
    ...
    .duplicateConfigDetector((List<Object> registered, Object newItem) -> {
         if (newItem isntanceof Some3rdPartyBundle) {
             // decide if item collide with provided registered instances (of the same type)
             return detectedDuplicate // instance that registered is duplicate to or null to accept item
         }           
         // allow instance registration
         return null;    
    })
```

Detector is called after equals check, so you may be sure that newItem is not equal to 
any of already registered instances (detector extends default deduplication mechanism, not replaces).      

Old *"1 instance per class"* behaviour could be recovered with bundled detector:

```java
.duplicateConfigDetector(new LegacyModeDuplicatesDetector())
```   

### Reporting

Configuration diagnostic report (`.printDiagnosticReport()`) was improved to show all registered instances and  
ignored duplicates.

For example, if we have module declared to be unique by constructor value:

```java
public class VMod extends AbstractModule {

    private int value;

    public VMod(int value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof VMod && value.equals(obj.value);
    }

}   
```   

If modules are registered like this:

```java
 GuiceBundle.builder()
    .modules(new VMod(1), new VMod(1), new VMod(2), new VMod(2))
    .printDiagnosticReport()
    .build()
```

Report would contain:

```
GUICE MODULES =
        VMod                          (com.mycompany) *REG(2/4) 

APPLICATION
    ├── module     VMod                          (com.mycompany)
    ├── module     -VMod                         (com.mycompany) *DUPLICATE
    ├── module     VMod#2                        (com.mycompany)
    ├── module     -VMod#2                       (com.mycompany) *DUPLICATE
```         

Where you can see that 2 of 4 registered modules of type VMod were registered.
Note that instances are numbered (#2) in order of registration (without duplicates) 
so you can always see what bundle were considered as original (and registration order when
bundles of the same type are registered in different bundles). 

### Configuration Model

As before, guicey configuration info (used for it's reports) is available through
`@Inject GuiceyConfigurationInfo info` bean. But in new version it is not class centric:
instead new `ItemId` class used.

ItemId compose item type with real object hash in order to differentiate different instances of type.

```java
// equal classes
ItemId.from(MyType.class).equals(ItemId.from(MyType.class))
// but not equal instances 
!ItemId.from(instance1).equals(ItemId.from(instance2))  
```

But, to simplify checks, instance id is always equal to type id:

```java
ItemId.from(myTypeInstance).equals(ItemId.from(MyType.class))  
```           

ItemId's now also used for scopes differentiation (because multiple bundles of the same type could be registered
and class is not enough anymore to differentiate scopes). 

Some api methods still return lists of classes (for cases when duplicates are not important).


## Dropwizard bundles direct support

Dropwizard bundles now could be registered directly with main guice bundle:

```java
 GuiceBundle.builder()
    .dropwizardBundles(new MyDropwizardBundle())
```

And from guicey bundle:

```java
public void initialize(GuiceyBootstrap bootstrap) {
    bootstrap.dropwizardBundles(new MyDropwizardBundle())
}
```

Benefits of registration through guicey api:

* Bundle could be disabled (with `.disableDropwizardBundles(MyDropwizardBundle.class)`)
* Bundle appear in diagnostic reports
* Deduplication mechanism applied to registered bundles

And that's not all: guicey will also track transitive dropwizard bundles
(bundles, installed by registered bundles). That means you can see (in report) 
and control entire application configuration.

!!! warning
    Only bundles registered with guicey api are tracked. If you don't want some bundle to be tracked
    register it in dropwizard boostrap object directly (even within guicey bundle bootstrap is available)

This could be useful for deduplication. For example, you have some common
dropwizard bundle and two other bundles, registering this common bindle. Normally,
you can't use both bundles, because dropwizard will try to install both common bundles.
But guicey will deduplicate them. 

!!! note
    Transitive dropwizard bundles are tracked by proxying bootsrap object, passed into bundles (not global!).
    This introduce ~200ms startup overhead (clearly visible on stats report).
    
Transitive drtopwizard bundles detection could be disabled with option:

```java
.option(GuieyOptions.TrackDropwizardBundles, false)
```

In this case only root bundles registered through guicey api would be visible for guicey
and so deduplication and disabling will affect only them.    

## Configuration from guice bindings (jersey1-guice style)

Guicey now can recognize extensions from guice bindings in configured guice modules. For example,

```java 
public class MyModule extends AbstractModule {
    public void configure () {
        // right parts just for example 
        bind(MyResource.class).to(MyResourceImpl.class);
        bind(MyManaged.class).toProvider(MyManagedProvider.class);
    }
}
```

Guicey will detect `MyResource` as jersey resource and `MyManaged` as managed extension.

This is completely equivalent to

```java
GuiceBundle.builder()
    .extensions(MyResource.class, MyManaged.class)
```

!!! note
    Internally, the same installers are used for extensions recognition and installation.
    The only difference is that guicey would not create default bindings for such extensions
    (because bindings already exists).


!!! warning "Limitations"
    Only bindings in user modules are checked: overriding modules are not checked as they supposed
    to be used for quick fixes and test mocking.
    Generified (`bind(new TypeLiteral<MyResource<String>(){})`) and 
    qualified (`bind(MyResource.class).annotatedWith(Qualify.class)`) bindings are ignored
    (simply because it's not obvious what to do with them).
    
Extensions from guice bindins could be also disabled:

```java
.disableExtensions(MyResource.class)
```

This is possible because guicey use guice SPI to parse configured bindings (before injector creation)
and could exclude some bindings before injector startup.

!!! note
    To avoid duplicate work by injector (to parse user modules again), guicey prepares synthetic
    module with pre-processed bindings (and pass it to injector instead of raw modules).
    
Also, guicey is able to track transitive modules (modules installed by registered modules)
and exclude disabled modules.      

For example, 

```java
public class MyModule extends AbstractModule {
    public void configure() {
        install(new OtherMyModule());
    }
}  

GuiceBundle.builder()
    .modules(new MyModule())
    .disableModules(OtherMyModule.class)
```

Will correctly exclude transitive module (including all later transitive modules).

!!! warning 
    Deduplication rules will not work with transitive modules: guicey can only detect
    complete guice modules tree, but not intercept module instance registration.
    Anyway, be aware that *guice itself* support deduplication: only one of equal modules
    will be actually installed.   
    
Guice modules analysis could be disabled with option:

```java
.option(GuiceyOptions.AnalyzeModules, false)
```

In this case, guicey will work exactly as before: it would only see root modules, configured
by user and extensions will not be detected from bindings. 

### Guice reports

Guice bindings will be shown on diagnostic report `.printDiagnosticReport()`

```
├── GUICE BINDINGS
    │   │
    │   └── BindModule                   (com.mycompany)
    │       └── extension  Ext                          (com.mycompany.ext)
```  

!!! note
    Guice bindings are shown as sub tree because only target extension module class is known
    whereas multiple modules with the same type could be registered.
    Extensions are show relative to the top guice module registered by user
    (because this report just shows "configuration sources")        


New guice bindings report `.printGuiceBindings()`:

```
 1 MODULES with 3 bindings
    │
    └── CasesModule                  (r.v.d.g.d.r.g.support)
        ├── <typelistener>                        CustomTypeListener                              at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.CasesModule.configure(CasesModule.java:19)
        ├── <provisionlistener>                   CustomProvisionListener                         at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.CasesModule.configure(CasesModule.java:26)
        ├── <aop>                                 CustomAop                                       at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.CasesModule.configure(CasesModule.java:33)
        ├── untargetted          [@Singleton]     AopedService                                    at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.CasesModule.configure(CasesModule.java:36) *AOP
        ├── linkedkey            [@Prototype]     BindService                                     at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.CasesModule.configure(CasesModule.java:37) *OVERRIDDEN
        └── instance             [@Singleton]     BindService2                                    at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.CasesModule.configure(CasesModule.java:38) *OVERRIDDEN


    1 OVERRIDING MODULES with 2 bindings
    │
    └── OverrideModule               (r.v.d.g.d.r.g.support)
        ├── linkedkey            [@Prototype]     BindService                                     at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.OverrideModule.configure(OverrideModule.java:16) *OVERRIDE
        └── instance             [@Singleton]     BindService2                                    at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.OverrideModule.configure(OverrideModule.java:17) *OVERRIDE


    1 UNDECLARED bindings
    └── JitService                   (r.v.d.g.d.r.g.s.exts)


    BINDING CHAINS
    └── BindService  --[linked]-->  OverrideService
``` 

Shows all bindings in user modules (without overriding modules)

!!! tip
    `.printAllGuiceBindings()` shows also guicey's own bindings and guice internal bindings.
    It may be useful to see everything guicey configures. 


And new guice aop map report `.printGuiceAopMap()`:

```
    2 AOP handlers declared
    ├── AopModule/Interceptor1                                                    at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.AopModule.configure(AopModule.java:23)
    └── AopModule/Interceptor2                                                    at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.AopModule.configure(AopModule.java:24)


    1 bindings affected by AOP
    │
    └── Service    (r.v.d.g.d.r.g.s.AopModule)
        ├── something()                                                       Interceptor1
        └── somethingElse(List)                                               Interceptor1
```    

Shows all declared guice aop handlers and how they apply to beans (including order).

!!! tip 
    This report is intended to be used as a tool to look some exact beans and methods with
    `.printGuiceAopMap(new GuiceAopConfig().types(...).methods(...))`

### BindingInstaller

Interface of `BindingInstaller` were changed from 

```java
public interface BindingInstaller {    
    <T> void bind(Binder binder, Class<? extends T> type);
}
```

into 

```java
public interface BindingInstaller {
    void bindExtension(Binder binder, Class<?> type, boolean lazy);
    <T> void checkBinding(Binder binder, Class<T> type, Binding<T> manualBinding);
    void installBinding(Binder binder, Class<?> type);
}
```

Where 

```java
void bindExtension(Binder binder, Class<?> type, boolean lazy);
``` 
will be called for classpath scan and directly registered extensions, 

```java
<T> void checkBinding(Binder binder, Class<T> type, Binding<T> manualBinding);
``` 

for detected guice bindings and 

```java
void installBinding(Binder binder, Class<?> type);
``` 

in both cases to apply some common logic (like reporting)

!!! warning
    Binding installers may now be called multiple times in case if guice reports will be activated.
    If required, actual run could be differentiated from report by current stage `currentStage() == Stage.TOOL`.
       


## Guicey listeners changes

Deduplication applied for registered listeners (through `.listen()`). It is not the same
mechanism as applied for extensions. Listeners are registered now in `LinkedHashSet` and so
listeners properly implementing equals and hashcode would be automatically ignored.

Listeners deduplication is useful for reporting: as now all guicey reports implemented 
with listeners and they correctly implement equals and hashcode so if user by accident
register multiple reports they would be printed just once.

Supported events were changed (due to lifecycle changes). More information 
(configuration objects) added to events. 

One special event added `ApplicationStarted` which is always called after dropwizard startup 
(including jersey start). It would be fired even in tests when `GuiceyAppRule` used 
(which does not start jersey). Supposed to be used to simplify diagnostic reporting.

## New shared configuration state

!!! warning "" This feature is intended to be used for very special cases only. It was added to 
remove current and future hacks for implementing global application state or bundles communication
and avoid testing side effects. 

Sometimes, it is required to share some "global" state between multiple bundles. In this cases
developer have to introduce static or TreadLocal hacks in order to share required state.
This usually leads to problems in tests where application instances may run concurrently.

Guicey provides now universal shared state mechanism. It is supposed to be used mainly by bundles.
For example, case with one main bundle (which register global scope) and other bundles, appending info into it:

```java
public class MainBundle implements GuiceyBundle {
    @Override
    public void initialize(GuiceyBootstrap bootstrap) {                   
        // make GlobalConfig instance available for other bundles
        bootstrap.shareState(MainBundle.class, new GlobalConfig());
    }
}                                                                

public class RelativeBundle implements GuiceyBundle {
    @Override
    publicvoid initialize(GuiceyBootstrap bootstrap) {                   
        // access global state
        GlobalConfig config = bootstrap.sharedStateOrFail(MainBundle.class, 
                "No global config found, register main bundle");
    }
}
```

Another example, when equal bundles need to share some context:
 
```java
public class SomeBundle implements GuiceyBundle {
    @Override
    public void initialize(GuiceyBootstrap bootstrap) {                   
        // either get shared config or register new one (first accessed bundle init state) 
        SharedConfig config = bootstrap
                .sharedState(SomeBundle.class, () -> new SharedConfig());
    }
}
```

Restrictions:

* State is accessed by class only to avoid dummy key mistakes. It is advised to use bundle name for key.
* State can be set only once to avoid hard to track re-assignments. Null not allowed.
* GuiceyBundle provide state assignment methods only in initialization phase. It is advised to 
    assign values only there to be sure value is available in run phase (avoid errors caused by registration order) 

Shared state could be accessed statically anywhere with application instance:

```java
GlobalConfig config = SharedConfigurationState
        .lookupOrFail(application, GlobalBundle.class, "Bundle not registered");
```

Guicey hooks could also access shared state with callback:

```java
public class XHook implements GuiceyConfigurationHook {
        @Override
        public void configure(GuiceBundle.Builder builder) {
            builder.withSharedState({
                it.put(XHook, "12")
            });
        }
    }
```   

But this is only useful for special states initialization (because bundles are not
executed yet). 

!!! note
    Guicey itself use this state for Injector storage and in extended bundles (gsp, spa). 


## Reporting changes

All guice reports were moved into top-level package "debug". All reports are
guicey listeners now. 

Listeners deduplication mechanism used to ignore duplicate reports activation:
e.g. duplicate `.printDiagnosticReport()` call will not cause duplicate report render.

Diagnostic report (`.printDiagnosticReport()`) is now logged as single log message
and not each sub report as separate log.

Statistics sub report now includes guice startup logs (previously required to be activated separately):

```
    ├── [54%] INJECTOR created in 236.6 ms
    │   ├── Module execution: 143 ms
    │   ├── Interceptors creation: 1 ms
    │   ├── TypeListeners & ProvisionListener creation: 2 ms
    │   ├── Scopes creation: 1 ms
    │   ├── Binding creation: 22 ms
    │   ├── Private environment creation: 1 ms
    │   ├── Binding initialization: 35 ms
    │   ├── Binding indexing: 1 ms
    │   ├── Collecting injection requests: 2 ms
    │   ├── Static validation: 4 ms
    │   ├── Instance member validation: 3 ms
    │   ├── Provider verification: 1 ms
    │   ├── Static member injection: 10 ms
    │   ├── Instance injection: 2 ms
    │   └── Preloading singletons: 6 ms
```   

`.printAvailableInstallers()` report shows installer markers by implemented interfaces (like `TypeInstaller`, 
`BindingInstaller`, etc.):

```
INSTALLERS in processing order = 
        OBJECT, ORDER                  lifecycle            (r.v.d.g.m.i.f.LifeCycleInstaller)     
        OBJECT, ORDER                  managed              (r.v.d.g.m.i.feature.ManagedInstaller) 
        OBJECT                         jerseyfeature        (r.v.d.g.m.i.f.j.JerseyFeatureInstaller) 
        JERSEY, BIND, OPTIONS          jerseyprovider       (r.v.d.g.m.i.f.j.p.JerseyProviderInstaller) 
        TYPE, JERSEY, BIND, OPTIONS    resource             (r.v.d.g.m.i.f.j.ResourceInstaller)    
        BIND                           eagersingleton       (r.v.d.g.m.i.f.e.EagerSingletonInstaller) 
        OBJECT                         healthcheck          (r.v.d.g.m.i.f.h.HealthCheckInstaller) 
        OBJECT                         task                 (r.v.d.g.m.i.feature.TaskInstaller)    
        BIND                           plugin               (r.v.d.g.m.i.f.plugin.PluginInstaller) 
        OBJECT, OPTIONS, ORDER         webservlet           (r.v.d.g.m.i.f.w.WebServletInstaller)  
        OBJECT, ORDER                  webfilter            (r.v.d.g.m.i.f.web.WebFilterInstaller) 
        OBJECT, OPTIONS, ORDER         weblistener          (r.v.d.g.m.i.f.w.l.WebListenerInstaller) 
```

If you were using `DiagnosticBundle` directly it is now `ConfigurationDiagnostic` listener.
`DebugGuiceyLifecycle` was renamed to `LifecycleDiagnostic`


## Guicey hooks changes

Guicey lifecycle hooks initially were supposed to be used for testing only. Now they
supposed to be also used for pluggable diagnostic tools (bundled, but not active). 
It could be additional tracing, instrumentation etc. 

Hooks supposed to be activated through system property:

```
-Dguicey.hooks=com.mycompany.MyHook1,com.mycompany.MyHook2
```

Hook aliases were introduced to simplify activation:

```java
GuiceBindle.builder()
    .hookAlias("alias1", MyHook1.class)    
```

After alias registration, hook(s) could be enabled with alias name instead of full class name:

```
-Dguicey.hooks=alias1
```

Also, to make available tools more obvious, all registered aliases are logged at startup:

```
INFO  [2019-09-16 16:26:35,229] ru.vyarus.dropwizard.guice.hook.ConfigurationHooksSupport: Available hook aliases [ -Dguicey.hooks=alias ]: 

    alias1                    com.mycompany.MyHook1

```   

By default, guicey provide `DiagnosticHook` which activates guicey's diagnostic reports:

```java
public class DiagnosticHook implements GuiceyConfigurationHook {
    public void configure(final GuiceBundle.Builder builder) {
        builder.printDiagnosticInfo()
                .printLifecyclePhasesDetailed()
                .printCustomConfigurationBindings()
                .printGuiceBindings();
    }
}
```

It is registered with `diagnostic` alias and so diagnostic reports may be easily enabled even
for compiled application with:

``` 
-Dguicey.hooks=diagnostic
```


## Multiple classloaders support

!!! note ""
    This is very special case, not appearing in usual dropwizard usage.

Before, guicey configuration was based on classes, but if same class come from
different classloaders, it would be detected as different extensions.

Now guicey would use class name for extension duplicates detection. 

The same for instances (modules, bundles): custom duplicates detector implementation (`.duplicateConfigDetector()`) will receive 
objects for test, even if object classes were loaded by different classloaders.

`UniqueGuiceyBundle` and `UniqueModule` classes equals and hashcode implementation
also check class names to correctly detect different classloaders case. 

!!! note ""
    Bundled reporting listeners still work on class level. Anyway custom listener
    could always implement equals and hashcode correctly honoring different classloaders case.     


## Guicey BOM includes guicey itself

Before, when guicey pom was used as bom, it was always required to put guicey version:

```groovy
dependencyManagement {
    imports.mavenBom 'ru.vyarus:dropwizard-guicey:4.2.2' 
}

dependencies {
    compile 'ru.vyarus:dropwizard-guicey:4.2.2'
}
```

Now guicey itself is included and only one version declaration required:

```groovy
dependencyManagement {
    imports.mavenBom 'ru.vyarus:dropwizard-guicey:5.0.0' 
}

dependencies {
    compile 'ru.vyarus:dropwizard-guicey'
}
```  

!!! warning
    Dropwizard bom was split into `dropwizard-bom` and `dropwizard-dependencies` so in order
    to override dropwizard version you will need to provide two dependencies:
    ```groovy
    dependencyManagement {
        imports {
            mavenBom 'ru.vyarus:dropwizard-guicey:5.0.0' 
            // override dropwizard version 
            mavenBom 'io.dropwizard:dropwizard-bom:2.0.1'
            mavenBom 'io.dropwizard:dropwizard-dependencies:2.0.1' 
        }
    }
    ```    

## Java 11 compatibility

Guicey is binary compatible with java 11. 
Declared `Automatic-Module-Name`: `dropwizard-guicey.core` (META-INF).

Still, guicey releases will be build with java 8, but CI tools will detect any future incompatibilities.


## Fixes issues                             

* [#60](https://github.com/xvik/dropwizard-guicey/issues/60) Fix configuration bindings for recursive configuration object declarations


## Migration guide

!!! warning
    In dropwizard 2.0 `@Context` fields injection [will not work for instances](https://github.com/dropwizard/dropwizard/issues/2781).
    As guicey register instances, you may be affected. Use `Provider<Something>` instead or rely
    on `@Context` injection for method parameters which is working.  

### Dropwizard related

Jersey abstracted DI code and so most of it's api classes changed. 
Dropwizard deprecate `Bundle` interface.

Migration matrix:

Old class | New class
----------|----------
org.glassfish.hk2.utilities.binding.AbstractBinder | org.glassfish.jersey.internal.inject.AbstractBinder
org.glassfish.hk2.utilities.Binder | org.glassfish.jersey.internal.inject.Binder
org.glassfish.hk2.api.Factory | java.util.function.Supplier
Factory used for Auth (user provider) | java.util.function.Function<ContainerRequest, ?>
org.glassfish.jersey.server.internal.process.AsyncContext | org.glassfish.jersey.server.AsyncContext     
org.glassfish.jersey.server.internal.inject.AbstractValueFactoryProvider | org.glassfish.jersey.server.internal.inject.AbstractValueParamProvider                         
org.glassfish.jersey.server.spi.internal.ValueFactoryProvider | org.glassfish.jersey.server.spi.internal.ValueParamProvider
org.glassfish.hk2.api.InjectionResolver | org.glassfish.jersey.internal.inject.InjectionResolver
io.dropwizard.Bundle | io.dropwizard.ConfiguredBundle (note that interface methods are default now and may not be implemented)
io.dropwizard.util.Size | io.dropwizard.util.DataSize
org.glassfish.hk2.api.ServiceLocator | org.glassfish.jersey.internal.inject.InjectionManager
ru.vyarus.dropwizard.guice.module.installer.feature.jersey.HK2Managed | ru.vyarus.dropwizard.guice.module.installer.feature.jersey.JerseyManaged

### GuiceyBundle

As most of guicey initialization was moved into configuration dropwizard phase,
you will have to change all `GuiceyBundle`'s.

It now contains 2 methods (in accordance with dropwizard phases):

* `void initialize(GuiceyBootstrap bootstrap)`
* `void run(GuiceyEnvironment environment)` 

Move all registrations (extensions, modules, bundles) into first method. Guice modules may still be registered in 
`run` phase (for simplicity as guicey modules often required configuration values). 

### Multiple instances registration (bundles, modules)

!!! warning
    This change most likely will be the cause of your app behaviour change. So start investigating from here.
     
This version accepts registration of 
[multiple instances of bundles and modules of the same type](#multiple-bundles-and-modules-of-the-same-type-support) 
(before only 1 instance of type was allowed).  

If you rely on this deduplication behaviour then most likely you will have now
duplicate bundle or module registrations.

!!! important
    So if you see very strange behaviour on new version then recover old behavior with:
    ```java
    GuiceBundle.builder()
        .duplicateConfigDetector(new LegacyModeDuplicatesDetector())
    ```
    And check again. If weird behaviour disappear then you have duplicate configurationsб
    if not - check if it's [module analysis issue](#guicey-bindings-analysis)
    
In case of problems you can either stay in legacy mode or find the root cause.
To find duplicate registrations enable diagnostic reports:

```java
.printDiagnosticReports()
```      
    
In configuration summary section pay attention to `REG(n/m)` markers, like this:

```
GUICE MODULES =
        VMod                          (com.mycompany) *REG(2/4) 
```

In this example, guice module VMod was registered 4 timed, but only 2 instances were accepted.
Most likely in your case numbers would be equal (meaning all instances were used).  

Next, look into configuration tree in order to find configuration sources, and look for 
duplicated item registration. You should see something like:

```
    APPLICATION
    ├── module     VMod                       (com.mycompany) 
    ...   
    │   
    ├── SomeBundle                 (com.mycompany) 
    │   ├── module     VMod#2                    (com.mycompany)
    ...  
```    

After that it should be clear how multiple instances appear.

!!! note "Possible scenario"
    As an example of what can happen: if you're using bundles lookup and were assuming before
    that if bundle will be registered manually - lookup will be simply ignored. But now it's not
    and multiple bundles will appear.

In order to fix multiple instances, correct equals method must be implemented.
Equals logic depends on your needs: you may need always only one instance, like before
or just remove instances with duplicate confiurations (with the same constructor parameters).

For "only 1 allowed" case, you can simply use provided `UniqueGuiceyBundle` or `UniqueModule` as base classes.
For example:

```java
public class VMod extends UiqueModule {...}
```         

or

Or you can simply properly implement equals method. For example, if you want only one instance of
some guice module to be used then implement it's equals as 

```java
public class VMod extends AbstractModule {
    ...
    
    @Override
    public boolean equals(final Object obj) {
        return obj != null && getClass().equals(obj.getClass());
    }
}
```   

And with this change, duplicates will be correctly avoided:

```
    APPLICATION
    ├── module     VMod                       (com.mycompany) 
    ...   
    │   
    ├── SomeBundle                 (com.mycompany) 
    │   ├── module     -VMod                    (com.mycompany) *DUPLICATE
    ...  
```

3rd party modules (where you can't implement equals method) may be deduplicated with

```java
.uniqueItems(Some3rdPathyModuel.class)
```

Or (for special cases) completely custom implementation could be provided.
E.g. suppose we can't change VMod and add correct equals, then:

```java
.duplicateConfigDetector((List<Object> registered, Object newItem) -> {
     if (newItem isntanceof VMod) {
         // decide if item collide with provided registered instances (of the same type)
         return registered.get(0);
     }           
     // allow instance registration
     return null;    
})
```

Detector in all cases when another instance of already registered type appear and
equals check did not reveal duplicates (custom config called only after check if new item is equal to 
any of registered items).

Detector receive all accepted instances of this type and new item. It must return either "original item"
to mark this new as duplicate for it, or null to allow registration.


### Guicey bindings analysis

!!! warning
    Another point of potential migration problems

As guicey now [analyze bindings from registered guice modules](#guicey-bindings-analysis) it could 
detect and install new extensions, declared in bindings. For example,

```java
public void configure() {
    bind(MyResource.class);
}
``` 

MyResource will now be recognized and installed as extension  (this affects all supported extension types).

Also, `.disableExtension()` and `.disableModule()` could now affect internal bindings.

For example, if before some "extension" was detected as bean in guice module (like above) and
you have `.desableExtensions(MyResource.class)` then bindings will disappear now (but before nothing happens).

The same for modules:

```java
public class MyModule extends AbstractModule {
    public void configure() {
        install(new OtherMyModule());
    }
}     

GuiceBundle.builder()
    .disableModules(OtherMyModule.class)    
```

Before, nothing happen and now `OtherMyModule` would be removed (all bindings of module).

!!! important 
    If you have strange behaviour and it's not caused by [configuration instances duplicates](#multiple-instances-registration-bundles-modules)
    then it's probably new modules processing. Recover old behaviour with
    ```java
    .option(GuiceyOptions.AnalyzeModules, false)
    ``` 
    And check again. If weird behaviour disappear then you have either new recognized extensions 
    or removed bindings (driven by disabled extensions or modules).

To investigate this problems use `.pringGuiceBindings()` report (with enabled analysis ofc.).
For example:
    
```java
public class TransitiveModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Ext1.class);
        bind(Ext2.class);
        install(new InnerModule());
    }

    public static class InnerModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(Ext3.class);
        }
    }
} 

GuiceBundle.builder()                     
    .modules(new TransitiveModule())
    .disableModules(InnerModule.class)
    .disableExtensions(Ext2.class)   
    .pringGuiceBindings()
```  

will report:

```
2 MODULES with 2 bindings
    │   
    └── TransitiveModule             (com.mycompany)    
        ├── untargetted          [@Prototype]     Ext1                                            at com.mycompany.TransitiveModule.configure(TransitiveModule.java:15) *EXTENSION
        ├── untargetted          [@Prototype]     Ext2                                            at com.mycompany.TransitiveModule.configure(TransitiveModule.java:16) *EXTENSION, REMOVED
        └── InnerModule                   (com.mycompany.TransitiveModule) *REMOVED
```

`InnerModule` and all of it's bindings were removed. `Ext2` was also removed (as disabled extension).

Or you can see only recognized bindings in configuration tree report `.printDiagnosticResport()`:

```
GUICE BINDINGS
        │   
        └── TransitiveModule             (com.mycompany)    
            ├── extension  Ext1                         (com.mycompany.TransitiveModule) 
            └── extension  -Ext2                        (com.mycompany.TransitiveModule) *DISABLED
```        

!!! note
    On diagnostic report extensions will appear below the top-level module
    because this report shows everything registered by user. Detailed location could
    be viewed in guice report (`.printGuiceBindings()`). 
    

### Test hooks

If you were using guicey hooks for testing, then you will have to rename:

* `GuiceyConfigurationRule` into `GuiceyHooksRule`
* `@UseGuiceyConfiguration` (spock extension) into `@UseGuiceyHooks`

### Removed options

`GuiceBundle.builder()#useWebInstallers()` was removed because `WebInstallersBundle` is 
activated by default and so you don't need to activate it anymore.

If you were using `GuiceyOptions.ConfigureFromDropwizardBundles` then consider using
`GuiceyBundle` now instead of combined dropwizard bundle (as guicey bundle is not limiting replacement now).

If you were using `GuiceyOptions.BindConfigurationInterfaces` then just annotate configuration
interfaces injection points with `@Config` (interface bindings are qualified).

### Reporting

If you were using `DiagnosticBundle` directly it is now `ConfigurationDiagnostic` listener.

`DebugGuiceyLifecycle` was renamed to `LifecycleDiagnostic`

All reports were moved into top-level package "debug".  

You don't need to enable guice statistics (injector creation) logs manually now: they would be 
intercepted automatically and show under diagnostics stats sub reprot (`.printDiagnosticReport()`)