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
MAIN /
    ├── filter     /custom/*                    CustomMappingFilter          (r.v.d.g.d.r.w.s.UserServletsBundle)          [ERROR]
    ├── filter     /async/*             async   AsyncFilter                  (r.v.d.g.d.r.w.s.UserServletsBundle)          [REQUEST]
    ├── filter     /both/*                      BothFilter                   (r.v.d.g.d.r.w.s.UserServletsBundle)          [REQUEST]
    ├── filter     /1/*                         MainFilter                   (r.v.d.g.d.r.w.s.UserServletsBundle)          [REQUEST]
    ├── filter     /2/*                         --"--                                                                      
    │   
    ├── filter     /*                   async   GuiceFilter                  (c.g.inject.servlet)                          [REQUEST]
    │   ├── guicefilter     /1/*                         GFilter                        r.v.d.g.d.r.w.support.GuiceWebModule
    │   ├── guicefilter     /1/abc?/.*           regex   GRegexFilter                   r.v.d.g.d.r.w.support.GuiceWebModule
    │   ├── guicefilter     /1/foo                       instance of GFilterInstance    r.v.d.g.d.r.w.support.GuiceWebModule
    │   ├── guiceservlet    /2/*                         GServlet                       r.v.d.g.d.r.w.support.GuiceWebModule
    │   ├── guiceservlet    /2/abc?/             regex   GRegexServlet                  r.v.d.g.d.r.w.support.GuiceWebModule
    │   └── guiceservlet    /2/foo                       instance of GServletInstance   r.v.d.g.d.r.w.support.GuiceWebModule
    │   
    ├── filter     /*                   async   AllowedMethodsFilter         (i.d.jersey.filter)                           [REQUEST]
    ├── filter     /*                   async   ThreadNameFilter             (i.d.servlets)                                [REQUEST]
    │   
    ├── servlet    /foo                         MainServlet                  (r.v.d.g.d.r.w.s.UserServletsBundle)      
    │   └── filter                                  TargetServletFilter          (r.v.d.g.d.r.w.s.UserServletsBundle)          [REQUEST]
    │   
    ├── servlet    /bar                         --"--                                                                  
    ├── servlet    /both                        BothServlet                  (r.v.d.g.d.r.w.s.UserServletsBundle)      
    ├── servlet    /async               async   AsyncServlet                 (r.v.d.g.d.r.w.s.UserServletsBundle)      
    ├── servlet    /*                   async   JerseyServletContainer       (i.d.jersey.setup)                        
    └── servlet    /                    async   Default404Servlet            (o.e.j.s.ServletHandler)                  


    ADMIN /
    ├── filter     /both/*                      BothFilter                   (r.v.d.g.d.r.w.s.UserServletsBundle)          [REQUEST]
    ├── filter     /1/*                         AdminFilter                  (r.v.d.g.d.r.w.s.UserServletsBundle)          [REQUEST]
    ├── filter     /2/*                         --"--                                                                      
    │   
    ├── filter     /*                   async   AdminGuiceFilter             (r.v.d.g.m.i.internal)                        [REQUEST]
    │   ├── guicefilter     /1/*                         GFilter                        r.v.d.g.d.r.w.support.GuiceWebModule
    │   ├── guicefilter     /1/abc?/.*           regex   GRegexFilter                   r.v.d.g.d.r.w.support.GuiceWebModule
    │   ├── guicefilter     /1/foo                       instance of GFilterInstance    r.v.d.g.d.r.w.support.GuiceWebModule
    │   ├── guiceservlet    /2/*                         GServlet                       r.v.d.g.d.r.w.support.GuiceWebModule
    │   ├── guiceservlet    /2/abc?/             regex   GRegexServlet                  r.v.d.g.d.r.w.support.GuiceWebModule
    │   └── guiceservlet    /2/foo                       instance of GServletInstance   r.v.d.g.d.r.w.support.GuiceWebModule
    │   
    ├── filter     /*                   async   AllowedMethodsFilter         (i.d.jersey.filter)                           [REQUEST]
    ├── servlet    /tasks/*             async   TaskServlet                  (i.d.servlets.tasks)                      
    ├── servlet    /fooadmin                    AdminServlet                 (r.v.d.g.d.r.w.s.UserServletsBundle)      
    ├── servlet    /baradmin                    --"--                                                                  
    ├── servlet    /both                        BothServlet                  (r.v.d.g.d.r.w.s.UserServletsBundle)      
    ├── servlet    /*                   async   AdminServlet                 (c.c.metrics.servlets)                    
    └── servlet    /                    async   Default404Servlet            (o.e.j.s.ServletHandler)     
```     

Report shows both main and admin contexts.

For both filters and servlets async [filters](../../installers/filter.md#async) and 
[servlets](../../installers/servlet.md#async) are explicitly marked with `async`.

Filters dispatch types are shown at the end of filter line:

```
├── filter     /custom/*                    CustomMappingFilter          (r.v.d.g.d.r.w.s.UserServletsBundle)          [ERROR]
├── filter     /async/*             async   AsyncFilter                  (r.v.d.g.d.r.w.s.UserServletsBundle)          [REQUEST]
```         

If filter is applied by servlet name then it would be rendered *below* target servlet:

```
├── servlet    /foo                         MainServlet                  (r.v.d.g.d.r.w.s.UserServletsBundle)      
    └── filter                                  TargetServletFilter          (r.v.d.g.d.r.w.s.UserServletsBundle)          [REQUEST]
```  

!!! warning
    Filters, applied by servlet name are not shown at all if target servets are not registered.
    
If filter or servlet is applied with multiple target urls then each pattern will start on new line and
only on first line complete information will be shown (idem `--"--` string will be used to identify same filter):

```
├── filter     /1/*                         MainFilter                   (r.v.d.g.d.r.w.s.UserServletsBundle)          [REQUEST]
├── filter     /2/*                         --"--  
```

## Guice 

Guice servlets and filters (declared in `ServletModule`s) are shown below guice `GuiceFilter`
(guice filter actually intercept requests and then manually redirect to matching guice bean):

``` 
├── filter     /*                   async   GuiceFilter                  (c.g.inject.servlet)                          [REQUEST]
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

Report also indicate all stopped and disabled items:

```
    MAIN /
    ├── filter     /custom/*                    CustomMappingFilter          (r.v.d.g.d.r.w.s.UserServletsBundle)          [ERROR]
    ├── filter     /async/*             async   AsyncFilter                  (r.v.d.g.d.r.w.s.UserServletsBundle)          [REQUEST]
    ├── filter     /both/*                      BothFilter                   (r.v.d.g.d.r.w.s.UserServletsBundle)          [REQUEST]
    ├── filter     /1/*                         MainFilter                   (r.v.d.g.d.r.w.s.UserServletsBundle)          [REQUEST]
    ├── filter     /2/*                         --"--
    │
    ├── servlet    /foo                         MainServlet                  (r.v.d.g.d.r.w.s.UserServletsBundle) *DISABLED STOPPED
    │   └── filter                                  TargetServletFilter          (r.v.d.g.d.r.w.s.UserServletsBundle)          [REQUEST]
    │
    ├── servlet    /bar                         --"--
    ├── servlet    /both                        BothServlet                  (r.v.d.g.d.r.w.s.UserServletsBundle)      STOPPED
    └── servlet    /async               async   AsyncServlet                 (r.v.d.g.d.r.w.s.UserServletsBundle)
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
    