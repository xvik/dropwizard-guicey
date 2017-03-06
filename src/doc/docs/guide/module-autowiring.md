# Module autowiring   

Because guice modules are registered in dropwizard init section only `Bootstrap` instance is available.
Often `Environment` and `Configuration` objects are also required.

## Autowiring interfaces 

Guicey can automatically inject environment and configuration objects into your module if 
it implements any of (or all of them): `BootstrapAwareModule`, `EnvironmentAwareModule` 
and `ConfigurationAwareModule` interfaces. 

Reference object will be set to module just before injector creation, so you can use it inside your 
module logic (`configuration` method).

!!! warning
    Module autowiring will only work for modules directly set to `modules()` (of main bundle or any guicey bundle).

```java
public class MyModule implements EnvironmentAwareModule {
    private Environemnt environment;
    
    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
    
    @Override
    protected void configure() {
        // use environment here
    }
}
```

## Autowiring base class

To avoid manually implementing interfaces (avoid boilerplate) you can use `DropwizardAwareModule` as 
base class which already implements all autowiring interfaces:

```java
public class MyModule extends DropwizardAwareModule<MyConfiguration> {
    @Override
    protected void configure() {
        bootstrap()     // Bootstrap instance
        environment()   // Environment instance
        configuration() // MyConfiguration instance
        appPackage()    // application class package 
    }
} 
```
