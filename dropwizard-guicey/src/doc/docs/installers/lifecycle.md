# Lifecycle installer

!!! summary ""
    CoreInstallersBundle / [LifeCycleInstaller](https://github.com/xvik/dropwizard-guicey/tree/master/dropwizard-guicey/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/LifeCycleInstaller.java)

Installs [jetty LifeCycle](https://javadoc.jetty.org/jetty-9/org/eclipse/jetty/util/component/LifeCycle.html) implementations.

## Recognition

Detects classes implementing the jetty `#!java LifeCycle` interface and registers their instances in the environment.

```java
public class MyCycle implements LifeCycle {
    ...
}
```

In most cases it's better to use [managed object](managed.md) instead of implementing lifecycle.

!!! tip
    Use Guicey `#!java @Order` annotation to order managed objects.
    ```java
    @Order(10)
    public class MyCycle implements LifeCycle
    ```
