### Default installers re-cofniguration sample

Guicey register many [installers](http://xvik.github.io/dropwizard-guicey/5.0.0/guide/installers/) by default:

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

But you can disable all of them with `.noDefaultInstallers()` and specify only 
required installers.

In this example, all installers except resource are disabled, so only rest resources
would be recognized and installed by guicey:

```java
GuiceBundle.builder()   
    .noDefaultInstallers()
    .installers(ResourceInstaller.class)
    .extensions(SampleResource.class)
    // see all registered installers
    .printAvailableInstallers()     
    .build()
```

```
    INSTALLERS in processing order = 
        TYPE, JERSEY, BIND, OPTIONS    resource             (r.v.d.g.m.i.f.j.ResourceInstaller)   
```


Also see sample spock tests using both [GuiceyAppRule](https://github.com/xvik/dropwizard-guicey#testing) (start only guice context - very fast) and 
[DropwizardAppRule](http://www.dropwizard.io/1.0.0/docs/manual/testing.html) (when http server started).