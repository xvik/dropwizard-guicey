# AOP report

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
