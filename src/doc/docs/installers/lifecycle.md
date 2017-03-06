# Lifecycle installer

!!! summary ""
    CoreInstallersBundle / [LifeCycleInstaller](https://github.com/xvik/dropwizard-guicey/tree/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/LifeCycleInstaller.java)        

Installs [jetty LifeCycle](http://download.eclipse.org/jetty/stable-9/apidocs/org/eclipse/jetty/util/component/LifeCycle.html) implementations.

## Recognition

Detects classes implementing jetty `#!java LifeCycle` interface and register their instances in environment.

```java
public class MyCycle implements LifeCycle {
    ...
}
```

In most cases it's better to use [managed object](managed.md) instead of implementing lifecycle.

!!! tip 
    Use guicey `#!java @Order` annotation to order managed objects.    
    ```java
    @Order(10)
    public class MyCycle implements LifeCycle
    ```
