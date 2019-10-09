# Guice bindings

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