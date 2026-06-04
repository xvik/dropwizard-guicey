# Managed installer

!!! summary ""
    CoreInstallersBundle / [ManagedInstaller](https://github.com/xvik/dropwizard-guicey/tree/master/dropwizard-guicey/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/ManagedInstaller.java)

Installs [Dropwizard managed objects](https://www.dropwizard.io/en/release-5.0.x/manual/core.html#managed-objects).

## Recognition

Detects classes implementing Dropwizard `#!java Managed` and registers their instances in the environment.

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

It is perfect for implementing the Guice [service lifecycle](https://github.com/google/guice/wiki/ModulesShouldBeFastAndSideEffectFree).

!!! tip
    Alternatively, you can use `@PostConstruct` and `@PreDestroy` annotations inside Guice beans
    with [lifecycle-annotations](../extras/lifecycle-annotations.md) extension module.

!!! tip
    Use Guicey `#!java @Order` annotation to order managed objects.
    ```java
    @Order(10)
    public class MyService implements Managed
    ```

