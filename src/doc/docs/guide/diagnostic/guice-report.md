# Guice bindings report

Guice bindings report show all available guice bindings.

```java
GuiceBundle.builder()
    ...
    .printGuiceBindings()
     // or printAllGuiceBindings() to see also guicey bindings (from GuiceBootstrapModule) 
    .build()
```     

Example report:

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

!!! note
    If you run application in IDE then binding traces (on the right) should be clickable in console
    (strange format of "at + full class name" used exactly to activate such links because IDE will consider
    these lines as stacktrace elements).     

Report is build using guice SPI from raw modules and that's why it shows everything
that modules configure (listeners, aop) 

!!! note
    You may use multiple modules of the same type (e.g. `.modules(new MyModule(), new MyModule())`)
    but report will always show each module only once (because it's impossible to know exact 
    instance for binding, only module class). 
    
Report contains 4 sections:

* Bindings of modules (registered with `.modules()`)
* Overriding bindings (from modules in `.modulesOverride()`)
* Undeclared bindings - JIT bindings (not declared in modules, but existing in runtime due to injection request)
* Binding chains. In most cases it's connection of interface to implementation like `bind(MyIface.class).to(MyIFaceImpl.class)`.
Especially useful for binding overrides because show actual chain (with applied override).    

Used markers:

* `EXTENSION` - recognized extension binding
* `REMOVED`- extension or module disabled and binding(s) removed
* `AOP` - bean affected by guice AOP
* `OVERRIDDEN` (only in modules tree) - binding is overridden with binding from overriding module
* `OVERRIDES` (only in overriding modules tree) - binding override something in main modules
* `WEB` - indicates modules extending `ServletModule`</li>
* `PRIVATE` - indicates private module

## Binding types

!!! note
    All non-binding declarations (listeners, aop handlers) are put into square braces
    (e.g. `lt;typelistener>)

Name        |  Example
------------|---------
&lt;scope>     | `#!java bindScope(..)`
&lt;aop>     | `#!java bindInterceptor(..)`
&lt;typelistener>     | `#!java bindListener(..)`
&lt;provisionlistener>     | `#!java bindListener(..)`
&lt;typeconverter>     | `#!java convertToTypes(..)`
&lt;filterkey>     | `#!java filter("/1/*").through(MyFilter.class)` (in `ServletModule`)
&lt;filterinstance>     | `#!java filter("/1/*").through(new MyFilter())` (in `ServletModule`)
&lt;servletkey>     | `#!java serve("/1/foo").with(MyServlet.class)` (in `ServletModule`)
&lt;servletinstance>     | `#!java serve("/1/foo").with(new MyServlet())` (in `ServletModule`)
instance     | `#!java bing(Smth.class).toInstance(obj)`
providerinstance | `#!java bind(Smth.class).toProvider(obj)`
linkedkey | `#!java bind(Smth.class).to(Other.class)` (`Other` may be already declared with separate binding)
providerkey | `#!java bind(Smth.class).toProvider(DmthProv.class)`
untargetted | `#!java bind(Smth.class)`  
providermethod     | Module method annotated with `#!java @Provides`
exposed | `#!java expose(PrivateService.class)` (service expose in `PrivateModule`) 

!!! warning
    Multibindings extension bindings are showed as raw bindings. There are no
    special support to aggregate them (maybe will be added later). 
    
    ```java
     MultibindingsModule          (r.v.d.g.d.r.g.support)    
            ├── linkedkey            [@Prototype]     @Element Plugin (multibinding)                  at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.MultibindingsModule.configure(MultibindingsModule.java:17)
            ├── instance             [@Singleton]     @Element Plugin (multibinding)                  at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.MultibindingsModule.configure(MultibindingsModule.java:18)
    ```
    
    for
    
    ```java
    Multibinder.newSetBinder(binder(), Plugin.class).addBinding().to(MyPlugin.class)
    ```

## Overrides

Report explicitly marks overridden bindings (by overriding modules). For example, in the report
above, module binding

```
├── linkedkey            [@Prototype]     BindService                                     at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.CasesModule.configure(CasesModule.java:37) *OVERRIDDEN
``` 

is overridden by

```
├── linkedkey            [@Prototype]     BindService                                     at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.OverrideModule.configure(OverrideModule.java:16) *OVERRIDE
```

!!! important
    Report lines are long so you'll have to scroll report blocks to the right to see markers.
    
## Aop

Report shows all beans affected with aop:

```
├── untargetted          [@Singleton]     AopedService                                    at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.CasesModule.configure(CasesModule.java:36) *AOP
```

!!! tip
    Use [AOP report](aop-report.md) to see what exact handlers were applied and with what order.
    
## Private modules

For private guice modules report will show all internal bindings:

```
INFO  [2019-10-13 09:21:21,080] ru.vyarus.dropwizard.guice.debug.GuiceBindingsDiagnostic: Guice bindings = 

    4 MODULES with 4 bindings
    │   
    └── OuterModule                  (r.v.d.g.d.r.g.s.privt)    
        │   
        └── InnerModule                  (r.v.d.g.d.r.g.s.privt)    *PRIVATE
            ├── untargetted          [@Prototype]     InnerService                                    at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.privt.InnerModule.configure(InnerModule.java:14)
            ├── untargetted          [@Prototype]     OuterService                                    at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.privt.InnerModule.configure(InnerModule.java:15) *EXPOSED
            ├── exposed              [@Prototype]     OuterService                                    at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.privt.InnerModule.configure(InnerModule.java:17)
            │   
            └── Inner2Module                 (r.v.d.g.d.r.g.s.privt)    
                ├── untargetted          [@Prototype]     InnerService2                                   at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.privt.Inner2Module.configure(Inner2Module.java:14)
                │   
                └── Inner3Module                 (r.v.d.g.d.r.g.s.privt)    *PRIVATE
                    └── untargetted          [@Prototype]     OutServ                                         at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.privt.Inner3Module.configure(Inner3Module.java:13) *EXPOSED

```        

where

```java 
public class OuterModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new InnerModule());
    }
}

public class InnerModule extends PrivateModule {
    @Override
    protected void configure() {
        install(new Inner2Module());
        bind(InnerService.class);
        bind(OuterService.class);

        expose(OuterService.class);
    }   
}      

public class Inner2Module extends AbstractModule {
    @Override
    protected void configure() {
        install(new Inner3Module());
        bind(InnerService2.class);
    }                                             
}

public class Inner3Module extends PrivateModule {

    @Override
    protected void configure() {
        bind(OutServ.class);
        expose(OutServ.class);
    }
}
```        

!!! warning 
    Exposed service from inner private module `expose(OutServ.class);` is not shown in report!
    It is hidden intentionally to clarify which bindings are visible by application (only exposed 
    bindings after the top-most private module).
    
## Removed bindings

For recognized extensions and transitive modules guicey can apply disable rules:
so when you disable extension (`.disableExtensions`) or guice module (`.disableModules`)
it will physically remove relative bindings. All removed are indicated on report.

For example:

```
INFO  [2019-10-13 09:26:59,502] ru.vyarus.dropwizard.guice.debug.GuiceBindingsDiagnostic: Guice bindings = 

    2 MODULES with 2 bindings
    │   
    └── TransitiveModule             (r.v.d.g.d.r.g.support)    
        ├── untargetted          [@Prototype]     Res1                                            at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.TransitiveModule.configure(TransitiveModule.java:15) *EXTENSION
        ├── untargetted          [@Prototype]     Res2                                            at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.TransitiveModule.configure(TransitiveModule.java:16) *EXTENSION, REMOVED
        └── Inner                        (r.v.d.g.d.r.g.s.TransitiveModule) *REMOVED
```       

where:

```java
public class TransitiveModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Res1.class);
        bind(Res2.class);
        install(new Inner());
    }

    public static class Inner extends AbstractModule {
        @Override
        protected void configure() {
            ...
        }
    }
}      


GuiceBundle.builder()
        .modules(new TransitiveModule())
        .disableExtensions(TransitiveModule.Res2)
        .disableModules(TransitiveModule.Inner)
        .printGuiceBindings()
        .build()
```      

## Report customization

Report is implemented as guicey [event listener](../events.md) and provide additional customization 
options, so if default configuration (from shortcut methods above) does not fit your needs
you can register listener directly with required configuration.
 
For example, guice bindings report without library bindings is configured like this:
 
```java
listen(new GuiceBindingsDiagnostic(new GuiceConfig()
                    .hideGuiceBindings()
                    .hideGuiceyBindings())
```    