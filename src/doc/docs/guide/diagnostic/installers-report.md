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

!!! important
    Comparing to complete diagnostic, it shows all installers (*even not used*). 
    In diagnostic reporting not used installers are hidden, because usually it means they are not needed.
