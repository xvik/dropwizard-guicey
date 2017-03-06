# Managed installer

!!! summary ""
    CoreInstallersBundle / [ManagedInstaller](https://github.com/xvik/dropwizard-guicey/tree/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/ManagedInstaller.java)        

Installs [dropwizard managed objects](http://www.dropwizard.io/1.0.6/docs/manual/core.html#managed-objects).

## Recognition

Detects classes implementing dropwizard `#!java Managed` and register their instances in environment.

```java
public class MyService implements Managed {

    @Override
    public void start() throws Exception {
        ...
    }

    @Override
    public void stop() throws Exception {
        ...
    }
}
```

It is perfect for implementing guice [service lifecycle](https://github.com/google/guice/wiki/ModulesShouldBeFastAndSideEffectFree).

!!! tip 
    Use guicey `#!java @Order` annotation to order managed objects.
    ```java
    @Order(10)
    public class MyService implements Managed
    ```

