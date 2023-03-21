# Installers report

Installers report is intended to show what extensions could be used and share some light on
how installers work.

Activation:

```java
GuiceBundle.builder() 
    ...
    .printAvailableInstallers()
    .build());
```

Example output:

```
INFO  [2019-10-11 06:09:06,085] ru.vyarus.dropwizard.guice.debug.ConfigurationDiagnostic: Available installers report

---------------------------------------------------------------------------[CONFIGURATION]

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


---------------------------------------------------------------------------[CONFIGURATION TREE]

    APPLICATION
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
        ├── installer  PluginInstaller              (r.v.d.g.m.i.f.plugin)     
        │   
        └── WebInstallersBundle          (r.v.d.g.m.installer)      
            ├── installer  WebFilterInstaller           (r.v.d.g.m.i.f.web)        
            ├── installer  WebServletInstaller          (r.v.d.g.m.i.f.web)        
            └── installer  WebListenerInstaller         (r.v.d.g.m.i.f.w.listener)      
```

!!! note
    This is actually re-configured [configuration report](configuration-report.md).
    But, in contrast to configuration report, it shows all installers (*even not used*). 

Also, it indicated used installer features. For example, looking at

```
OBJECT, ORDER                  managed              (r.v.d.g.m.i.feature.ManagedInstaller) 
```

You could see that managed installer (responsible for `Managed` objects installation)
use objects for installation (obtains extension instance from guice and registers it).
Also it supports ordering (so extensions could use `@Order` annotation). 

By looking  at `@EagerSingleton` installer:

```
BIND                           eagersingleton       (r.v.d.g.m.i.f.e.EagerSingletonInstaller)
```

You can see that it only applies guice binding and nothing more.

## Installer features

Feature | Description
----------|---------
`OBJECT` | Installer use object instances for extensions registration (obtain instance from guice context)
`TYPE` | Installer use extension class for extension registration. Usually it's jersey installers which has to register extension in jersey context
`JERSEY` | Installer installs jersey features (in time of jersey start and not after injector creation as "pure" installers)
`BIND` | Installer perform manual guice binding. For such installers, automatic untargeted binding for extension is not created (assuming installer require some custom binding). Such installers also verify manual guice bindings, when they are recognized as extension binding.
`OPTIONS` | Installer requires access for options. Most likely it means it supports additional configuration options (but it cold just read core options value).
`ORDER` | Installer supports extensions ordering. Use `@Order` annotation to declare order.               
