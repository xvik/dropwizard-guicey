# Modules analysis

Before injector start, guicey parse registered modules with guice SPI in order to:

* [Recognize extensions from bindings](#extensions-recognition)
* [Remove disabled extensions bindings](#disabled-extensions)
* [Remove bindings of disabled modules](#transitive-modules)

!!! tip
    Use [guice report](../diagnostic/guice-report.md) to see all available bindings

!!! warning
    Only direct modules (`.modules()`) are analyzed! Overriding modules (`.modulesOverride()`)
    are ignored (intentionally).  
    
Then all not removed bindings are composed to special module (even when no bindings removed,
this avoid duplicate modules parsing on injector creation). That means that [injector factory](injector.md) 
receive not user registered modules, but synthetic (with pre-parsed bindings) module instead. 

!!! note
    The most time-consuming operation in analysis is modules parsing, which is actually
    performed in any case during guice injector creation. You can see this on 
    [statistics report](../diagnostic/configuration-report.md#timings): if you [switch off analysis](#disabling-analysis)
    injector time will grow. 

## Extensions recognition

Guicey can recognize extensions in guice bindings (from configured guice modules). 
For example,

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

!!! tip
    Extensions annotated with `@InvisibleForScanner` are not recognized, like in [classpath scanner](../scan.md).
    But note that annotated extensions *should not be registered manually*! Because it will
    lead to default extension binding registration by guicey, which will most likely conflict with
    existing binding (as a workaround `@LazyBinding` annotation may be used).
    
    Alternatively, you can simply qualify bean and it would not be recognized as extension. 

This is completely equivalent to

```java
GuiceBundle.builder()
    .extensions(MyResource.class, MyManaged.class)
```

!!! note
    Internally, the same [installers](../installers.md) are used for extensions recognition and installation.
    The only difference is that guicey would not create default bindings for such extensions
    (because bindings already exists).

### Restrictions

* Only direct bindings (`bind(..)`) and linked keys (`bind(..).to(..)`) are checked.
* Instances are not analyzed (`bind(Something.class).toInstance(new Extension())`) because extensions supposed to be guice-managed 
    (not strictly required, but will allow to avoid aop-related problems). But, still
    it is possible to declare not guice managed extension with instance mapping to extension class or provider. 
* Generified (`bind(new TypeLiteral<MyResource<String>(){})`) and 
    qualified (`bind(MyResource.class).annotatedWith(Qualify.class)`) bindings are ignored
    (simply because it's not obvious what to do with them).
* [Overriding modules](override.md) are not checked  as they supposed to be used for quick fixes and test mocking.     

!!! success "Will be recognized"    
    ```java
    // untargeted binding
    bind(Extension.class)
    
    // left side of the link
    bind(Extension.class).to(Something.class)
    
    // right side of the link
    bind(Something.class).to(Extension.class)
    
    // left side of instance or provider mapping
    bind(Extension.class).toInstance(new Extension())
    bind(Extension.class).toProvider(SomeProvider.class)    
    ```

!!! fail "Will NOT be recognized"
    ```java
    // instances not analysed
    bind(Something.class).toInstance(new Extension())    
    
    // extension-recignizable type must be strictly declared 
    bind(Something.class).toProvider(ExtensionProvider.class)

    // generified declaration    
    bind(new TypeLiteral<Extension<Somthing>(){})

    // qualified declaration    
    bind(Extension.class).annotatedWith(Qualify.class)
    ```
    
!!! note "Side note"
    Qualified and generified cases are not supported because they imply that multiple
    instances of one class may be declared. This rise problems with direct manual declaration:
    for example, if user declare `.extensions(Extension.class)` and in module we have
    `bind(Extension.class).annotatedWith(Qualify.class)` how can we be sure if it is the same
    declaration or not? 
    
    Current implementation will not revognize qualified extension and automatically create
    direct binding (`bind(Extension.class)`).
    
    For sure someone will face generified or qualified extensions case, but it would be 
    simplier to workaraund it in exact case, rather then trying to handle all posible cases
    in general, making everything more complex.     

### Disabled extensions

In order to [disable extension](../disables.md#disable-extensions), recognized from binding,
guicey will simply remove this binding.

If extension was a part of longer links chain then entire chain would be removed too!

For example, 

```java
bind(One.class).to(Two.class)
bind(Two.class).to(Extension.class)
```

When `Extension` disabled `One-->Two` link is also removed.

Motivation: 

* First of all, this avoids error cases when remaining chain part contains 
only abstract types (e.g. only interfaces remains)
* Removes possible inconsistencies as long chains may appear due to some class overrides and so 
removing only top (overriding) class will just to "before override" state.  

Removed chains are visible on [guice report](#removed-bindings).

## Transitive modules

During bindings analysis guicey can see binding module's hierarchy (module "A" installs module "B", which registers binding C).
Using this guicey can remove all bindings relative to exact module class - the result is the same
as if such module was never registered.

This is [transitive modules disable](../disables.md#disable-guice-modules) implementation.
 

## Disabling analysis

To completely switch off analysis use option:

```java
.option(GuiceyOptions.AnalyzeModules, false)
```

!!! warning
    When analysis is disabled, [extensions recognition](#extensions-recognition) and 
    [transitive modules disables](../disables.md#disable-guice-modules) will not work anymore!    

With disabled analysis [injector factory](injector.md) will receive user provided modules directly (instead of pre-parsed synthetic module).

!!! important
    Enabled analysis completely prevent situations when default binding, created by guicey, conflict
    with manual binding existing in module. In such case startup will fail. Before modules analysis
    it was only possible to solve such issue with `@LazyBinding` annotation.  

## Reporting

You can see analysis information under [diagnostic report](../diagnostic/configuration-report.md):

```   
    ...

    ├── [9.3%] MODULES processed in 40.14 ms
    │   ├── 7 modules autowired
    │   ├── 8 elements found in 5 user modules in 36.53 ms
    │   └── 1 extensions detected from 3 acceptable bindings  

    ...

    ├── [0.70%] EXTENSIONS installed in 3.594 ms
    │   ├── 4 extensions installed
    │   └── declared as: 2 manual, 1 scan, 1 binding

    ...

    ├── GUICE BINDINGS
    │   │   
    │   └── ModuleWithExtensions         (r.v.d.g.d.s.module)       
    │       └── extension  ModuleFeature                (r.v.d.g.d.s.m.ModuleWithExtensions) 
    
    ...     
```  

Here you can see that `5 user modules` were analyzed out ot 7 overall modules. 2 avoided modules are
`GuiceBootstrapModule` and some overriding module.  

Modules contains `8 elements`: this includes not only bindings, but also type listeners, aop handlers, etc (all declarations).

`1 extensions detected from 3 acceptable bindings` - only 3 bindings were acceptable for analysis 
(not generified and not qualified bindings) and 1 extension was recognized.
Recognition could also be seen under `EXTENSIONS` section: `declared as: 2 manual, 1 scan, 1 binding`
(note that numbers show detections, but single extension may be detected in multiple sources).

And, finally, configuration tree shows [extension binding module](../diagnostic/configuration-report.md#guice-bindings). But it's always top-most
registered module (binding could be actually declared in some transitive module)!

### Removed bindings

If any bindings were removed, this would be also shown in report:

``` 
    ...

    ├── [11%] MODULES processed in 37.54 ms
    │   ├── 2 modules autowired
    │   ├── 4 elements found in 1 user modules in 32.43 ms
    │   ├── 2 extensions detected from 2 acceptable bindings
    │   ├── 3 elements removed
    │   └── 1 inner modules removed (types)

    ...
```         

(removed links are also counted)

[Guice bindings](../diagnostic/guice-report.md) report shows exact removed items:

```
2 MODULES with 2 bindings
    │   
    └── TransitiveModule             (r.v.d.g.d.r.g.support)    
        ├── untargetted          [@Prototype]     Res1                                            at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.TransitiveModule.configure(TransitiveModule.java:15) *EXTENSION
        ├── untargetted          [@Prototype]     Res2                                            at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.TransitiveModule.configure(TransitiveModule.java:16) *EXTENSION, REMOVED
        └── Inner                        (r.v.d.g.d.r.g.s.TransitiveModule) *REMOVED
```

Here entire module `Inner` and `Res2` extension binding removed.

Removed chains are shown as:

```
    BINDING CHAINS
    └── Base  --[linked]-->  Ext  --[linked]-->  ExtImpl       *CHAIN REMOVED
``` 
