# AOP report

Guice AOP report shows all registered aop handlers and how (what order) they apply to guice beans. 

```java
GuiceBundle.builder()
    ...
    .printGuiceAopMap()     
    .build()
```     
 
Example output:

```
    2 AOP handlers declared
    ├── AopModule/Interceptor1                                                    at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.AopModule.configure(AopModule.java:23)
    └── AopModule/Interceptor2                                                    at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.AopModule.configure(AopModule.java:24)


    1 bindings affected by AOP
    │
    └── Service    (r.v.d.g.d.r.g.s.AopModule)
        ├── something()                                                       Interceptor1
        └── somethingElse(List)                                               Interceptor1, Interceptor2
```    

This report shows all affected beans with all methods. In real application it may lead
to the giant report. In real life it would be more suitable to always fine-tune report [as a tool](#tool).

!!! important
    Report is build using guice SPI from raw modules because it's the only way to detect 
    registered aop listeners. But this also cause an additional execution of
    `configure()` method of all modules when report is enabled.

    In most cases, this is not a problem (modules should only declare bindings). Report use
    stage `TOOL`, so, if required, you can easilly avoid duplicate execution for sensitive logic: 
    ```java
    if (binder.currentStage() != Stage.TOOL) {
        // do only on normal run
    }
    ```
    For example, guicey itself use this in installers to avoid duplicate console output on startup when report enabled.

!!! warning "Kotlin"
    If you're using kotlin, keep in mind that it makes methods final by default so guice will not
    be able to [apply aop](https://github.com/google/guice/wiki/AOP#limitations).
    The workaround is to put **open** keyword [before method](https://discuss.kotlinlang.org/t/aop-and-kotlin/685/6).  
   
## Tool

!!! important
    Report is intended to be used as a tool in order to reveal aop on exact bean or even method
    (or showing appliances of exact handler).
    
Partial aop map activation:

```java
.printGuiceAopMap(new GuiceAopConfig()
                .types(...)
                .methods(...)
                .interceptors(...))
```     

Where usual guice matchers used for type and method matching.

For example, to see only bindings of exact bean:

```java
.printGuiceAopMap(new GuiceAopConfig()
            .types(Matchers.subclassesOf(MyService.class)))
```                   

All methods returning `List`:

```java 
.printGuiceAopMap(new GuiceAopConfig()
                .methods(Matchers.returns(Matchers.subclassesOf(List))))
```         

All appliances of exact interceptor:

```java
.printGuiceAopMap(new GuiceAopConfig()
                .hideDeclarationsBlock()
                .interceptors(MyInterceptor))
```       

!!! note
    In the last case still all handlers applied to filtered methods will be shown (to see overall picture).
    
## Report customization

Report is implemented as a guicey [event listener](../events.md), so you can register it directly 
in your bundle if required (without main bundle shortcuts):

```java     
listen(new GuiceAopDiagnostic(new GuiceAopConfig()));
```

Report rendering logic may also be used directly as report provide separate renderer object
implementing `ReportRenderer`. Renderer not bound to guice context and assume direct instantiation. 

For examples of direct renderer usage see [events](../events.md) implementation:

* `InjectorPhaseEvent.ReportRenderer` 
