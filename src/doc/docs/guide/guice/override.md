# Override guice bindings

Guice natively support bindings override with `Modules.override()` api. This is mostly 
useful in tests, when some bindings could be overridden with mocks. But it could also 
be used in real application in order to "workaround" some 3rd party module behaviour. 

Guicey provides special support for overridings registration.
 
You need 2 modules: one with original binding and another with 
binding override.

For example, suppose you want to replace `ServiceX` binding from:

```java
public class MyModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ServiceX.class).to(ServiceXImpl.class);        
    }
}
```
   
Generally have few options:

* If it implements an interface, implement your own service and bind as 
`#!java bind(ServiceX.class).to(MyServiceXImpl.class)`
* If service is a class, you can modify its behaviour with extended class
`#!java bind(ServiceX.class).to(MyServiceXExt.class)`
* Or you can simply register some mock instance
`#!java bind(ServiceX.class).toInstance(myMockInstance)`

Here assume that `ServiceX` is interface, so simply register different implementation:
 
```java      
public class MyOverridingModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ServiceX.class).to(CustomServiceXImpl.class);        
    }
}
```  

Now register overriding:

```java
GuiceBundle.builder()
    .modules(new MyModule())
    .modulesOverride(new MyOverridingModule())  
    .build()  
```

And everywhere in code `#!java @Inject ServiceX service;` will receive `CustomServiceXImpl`
(instead of `ServiceXImpl`)

!!! tip
    Sometimes it may be simpler to [disable existing module](../disables.md#disable-guice-modules)
    and register new module with modified bindings instead of overrides (for example with a [hooks](../hooks.md))
    
!!! note
    Overriding module could contain additional bindings - they would be also available in the resulted injector.
    (binding from overriding module either overrides existing binding or simply added as new binding)    
