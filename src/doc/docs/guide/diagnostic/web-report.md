# Web mappings report

Report shows all registered servlets and filters (including declared in guice `ServletModule`).

```java
GuiceBundle.builder()
    ...
    .printWebMappings() 
    .build()
```      

Example report:

```
INFO  [2019-10-24 07:48:32,601] ru.vyarus.dropwizard.guice.debug.WebMappingsDiagnostic: Web mappings: 

    MAIN /
    ├── filter     /custom/*                    CustomMappingFilter          (r.v.d.g.d.r.w.s.UserServletsBundle)                    [ERROR]         .custommapping
    ├── filter     /async/*             async   AsyncFilter                  (r.v.d.g.d.r.w.s.UserServletsBundle)                    [REQUEST]       .async
    ├── filter     /both/*                      BothFilter                   (r.v.d.g.d.r.w.s.UserServletsBundle)                    [REQUEST]       .both
    ├── filter     /1/*                         MainFilter                   (r.v.d.g.d.r.w.s.UserServletsBundle)                    [REQUEST]       .main
    ├── filter     /2/*                         --"--                                                                                                
    │   
    ├── filter     /*                   async   GuiceFilter                  (c.g.inject.servlet)                                    [REQUEST]       Guice Filter
    │   ├── guicefilter     /1/*                         GFilter                        r.v.d.g.d.r.w.support.GuiceWebModule
    │   ├── guicefilter     /1/abc?/.*           regex   GRegexFilter                   r.v.d.g.d.r.w.support.GuiceWebModule
    │   ├── guicefilter     /1/foo                       instance of GFilterInstance    r.v.d.g.d.r.w.support.GuiceWebModule
    │   ├── guiceservlet    /2/*                         GServlet                       r.v.d.g.d.r.w.support.GuiceWebModule
    │   ├── guiceservlet    /2/abc?/             regex   GRegexServlet                  r.v.d.g.d.r.w.support.GuiceWebModule
    │   └── guiceservlet    /2/foo                       instance of GServletInstance   r.v.d.g.d.r.w.support.GuiceWebModule
    │   
    ├── filter     /*                   async   AllowedMethodsFilter         (i.d.jersey.filter)                                     [REQUEST]       io.dropwizard.jersey.filter.AllowedMethodsFilter-5d51e129
    ├── filter     /*                   async   ThreadNameFilter             (i.d.servlets)                                          [REQUEST]       io.dropwizard.servlets.ThreadNameFilter-21c815e4
    │   
    ├── servlet    /foo                         MainServlet                  (r.v.d.g.d.r.w.s.UserServletsBundle)                                    target
    │   └── filter                                  TargetServletFilter          (r.v.d.g.d.r.w.s.UserServletsBundle)                    [REQUEST]       .targetservlet
    │   
    ├── servlet    /bar                         --"--                                                                                                
    ├── servlet    /both                        BothServlet                  (r.v.d.g.d.r.w.s.UserServletsBundle)                                    .both
    ├── servlet    /async               async   AsyncServlet                 (r.v.d.g.d.r.w.s.UserServletsBundle)                                    .async
    ├── servlet    /*                   async   JerseyServletContainer       (i.d.jersey.setup)                                                      io.dropwizard.jersey.setup.JerseyServletContainer-cf518cf
    └── servlet    /                    async   Default404Servlet            (o.e.j.s.ServletHandler)                                                org.eclipse.jetty.servlet.ServletHandler$Default404Servlet-5a6fa56e


    ADMIN /
    ├── filter     /both/*                      BothFilter                   (r.v.d.g.d.r.w.s.UserServletsBundle)                    [REQUEST]       .both
    ├── filter     /1/*                         AdminFilter                  (r.v.d.g.d.r.w.s.UserServletsBundle)                    [REQUEST]       .admin
    ├── filter     /2/*                         --"--                                                                                                
    │   
    ├── filter     /*                   async   AdminGuiceFilter             (r.v.d.g.m.i.internal)                                  [REQUEST]       Guice Filter
    │   ├── guicefilter     /1/*                         GFilter                        r.v.d.g.d.r.w.support.GuiceWebModule
    │   ├── guicefilter     /1/abc?/.*           regex   GRegexFilter                   r.v.d.g.d.r.w.support.GuiceWebModule
    │   ├── guicefilter     /1/foo                       instance of GFilterInstance    r.v.d.g.d.r.w.support.GuiceWebModule
    │   ├── guiceservlet    /2/*                         GServlet                       r.v.d.g.d.r.w.support.GuiceWebModule
    │   ├── guiceservlet    /2/abc?/             regex   GRegexServlet                  r.v.d.g.d.r.w.support.GuiceWebModule
    │   └── guiceservlet    /2/foo                       instance of GServletInstance   r.v.d.g.d.r.w.support.GuiceWebModule
    │   
    ├── filter     /*                   async   AllowedMethodsFilter         (i.d.jersey.filter)                                     [REQUEST]       io.dropwizard.jersey.filter.AllowedMethodsFilter-5c080ef3
    ├── servlet    /tasks/*             async   TaskServlet                  (i.d.servlets.tasks)                                                    tasks
    ├── servlet    /fooadmin                    AdminServlet                 (r.v.d.g.d.r.w.s.UserServletsBundle)                                    .admin
    ├── servlet    /baradmin                    --"--                                                                                                
    ├── servlet    /both                        BothServlet                  (r.v.d.g.d.r.w.s.UserServletsBundle)                                    .both
    ├── servlet    /*                   async   AdminServlet                 (c.c.metrics.servlets)                                                  com.codahale.metrics.servlets.AdminServlet-24e83d19
    └── servlet    /                    async   Default404Servlet            (o.e.j.s.ServletHandler)                                                org.eclipse.jetty.servlet.ServletHandler$Default404Servlet-1200458e     
```     

Report shows both main and admin contexts.

!!! note
    Filter and servlet names are shown list at each line because they are often 
    autogenerated and hardly readable. Still name is required if you will need to 
    map filter to exact servlet (or to disable/stop exact servlet) 

For both filters and servlets async [filters](../../installers/filter.md#async) and 
[servlets](../../installers/servlet.md#async) are explicitly marked with `async`.

Filters dispatch types are shown at the end of filter line:

```
├── filter     /custom/*                    CustomMappingFilter          (r.v.d.g.d.r.w.s.UserServletsBundle)                    [ERROR]         .custommapping
├── filter     /async/*             async   AsyncFilter                  (r.v.d.g.d.r.w.s.UserServletsBundle)                    [REQUEST]       .async
```         

If filter is applied by servlet name then it would be rendered *below* target servlet:

```
├── servlet    /foo                         MainServlet                  (r.v.d.g.d.r.w.s.UserServletsBundle)                                    target
│   └── filter                                  TargetServletFilter          (r.v.d.g.d.r.w.s.UserServletsBundle)                    [REQUEST]       .targetservlet
```  

!!! warning
    Filters, applied by servlet name are not shown at all if target servets are not registered.
    
If filter or servlet is applied with multiple target urls then each pattern will start on new line and
only on first line complete information will be shown (idem `--"--` string will be used to identify same filter):

```
├── filter     /1/*                         MainFilter                   (r.v.d.g.d.r.w.s.UserServletsBundle)                    [REQUEST]       .main
├── filter     /2/*                         --"--                                                                                                  
```

## Guice 

Guice servlets and filters (declared in `ServletModule`s) are shown below guice `GuiceFilter`
(guice filter actually intercept requests and then manually redirect to matching guice bean):

``` 
├── filter     /*                   async   GuiceFilter                  (c.g.inject.servlet)                                    [REQUEST]       Guice Filter
│   ├── guicefilter     /1/*                         GFilter                        r.v.d.g.d.r.w.support.GuiceWebModule
│   ├── guicefilter     /1/abc?/.*           regex   GRegexFilter                   r.v.d.g.d.r.w.support.GuiceWebModule
│   ├── guicefilter     /1/foo                       instance of GFilterInstance    r.v.d.g.d.r.w.support.GuiceWebModule
```           

!!! attention
    Guice servlets and filters are shown in both admin and main contexts, because `GuiceFilter` is applied on
    both contexts and so all urls will work in both contexts.
    
Note that regex registrations are explicitly markerd with `reges`

```java
filterRegex("/1/abc?/.*").through(GRegexFilter.class)
```

## State

Report also indicate all stopped and disabled items (report below executed under lightweight guicey test,
when jetty is not started):

```
    MAIN /
    ├── filter     /custom/*                    CustomMappingFilter          (r.v.d.g.d.r.w.s.UserServletsBundle)      STOPPED       [ERROR]         .custommapping
    ├── filter     /async/*             async   AsyncFilter                  (r.v.d.g.d.r.w.s.UserServletsBundle)      STOPPED       [REQUEST]       .async
    ├── filter     /both/*                      BothFilter                   (r.v.d.g.d.r.w.s.UserServletsBundle)      STOPPED       [REQUEST]       .both
    ├── filter     /1/*                         MainFilter                   (r.v.d.g.d.r.w.s.UserServletsBundle)      STOPPED       [REQUEST]       .main
    ├── filter     /2/*                         --"--
    │
    ├── filter     /*                   async   GuiceFilter                  (c.g.inject.servlet)                      STOPPED       [REQUEST]       Guice Filter
    │   ├── guicefilter     /1/*                         GFilter                        r.v.d.g.d.r.w.support.GuiceWebModule
    │   ├── guicefilter     /1/abc?/.*           regex   GRegexFilter                   r.v.d.g.d.r.w.support.GuiceWebModule
    │   ├── guicefilter     /1/foo                       instance of GFilterInstance    r.v.d.g.d.r.w.support.GuiceWebModule
    │   ├── guiceservlet    /2/*                         GServlet                       r.v.d.g.d.r.w.support.GuiceWebModule
    │   ├── guiceservlet    /2/abc?/             regex   GRegexServlet                  r.v.d.g.d.r.w.support.GuiceWebModule
    │   └── guiceservlet    /2/foo                       instance of GServletInstance   r.v.d.g.d.r.w.support.GuiceWebModule
    │
    ├── servlet    /foo                         MainServlet                  (r.v.d.g.d.r.w.s.UserServletsBundle)      STOPPED                       target
    │   └── filter                                  TargetServletFilter          (r.v.d.g.d.r.w.s.UserServletsBundle)      STOPPED       [REQUEST]       .targetservlet
    │
    ├── servlet    /bar                         --"--
    ├── servlet    /both                        BothServlet                  (r.v.d.g.d.r.w.s.UserServletsBundle)      STOPPED                       .both
    └── servlet    /async               async   AsyncServlet                 (r.v.d.g.d.r.w.s.UserServletsBundle)      STOPPED                       .async
```

Here `MainServlet` is disabled (and stopped) and `BothServlet` is just stopped.

!!! tip
    Servlet could be disabled like this:
    
    ```java
    environment.getApplicationContext().getServletHandler()
            .getServlet("target").setEnabled(false)
    ```    
    
## Report customization

Report is implemented as guicey [event listener](../events.md) and provide additional customization 
options, so if default configuration (from shortcut methods above) does not fit your needs
you can register listener directly with required configuration.

For example, same report by for main context only:

```java    
listen(new WebMappingsDiagnostic(new MappingsConfig()
                    .showMainContext()
                    .showDropwizardMappings()
                    .showGuiceMappings()))
```

Report rendering logic may also be used directly as report provide separate renderer object
implementing `ReportRenderer`. Renderer not bound to guice context and assume direct instantiation.    
    