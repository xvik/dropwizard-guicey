# Web listener installer

!!! summary ""
    WebInstallersBundle / [WebListenerInstaller](https://github.com/xvik/dropwizard-guicey/tree/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/web/listener/WebListenerInstaller.java)        

Register new web listener in main or admin contexts.

## Recognition

Detects classes annotated with `@javax.servlet.annotation.WebListener` annotation and register them in dropwizard environment.

```java
@WebListener
public class MyListener implements ServletContextListener, ServletRequestListener {...}
```

!!! note ""
    Listener could implement multiple listener interfaces and all types will be registered.

Supported listeners (the same as declared in annotation):

 * javax.servlet.ServletContextListener
 * javax.servlet.ServletContextAttributeListener
 * javax.servlet.ServletRequestListener
 * javax.servlet.ServletRequestAttributeListener
 * javax.servlet.http.HttpSessionListener
 * javax.servlet.http.HttpSessionAttributeListener
 * javax.servlet.http.HttpSessionIdListener


!!! warning ""
    By default, dropwizard is not configured to support sessions. If you define session listeners without configured session support
    then warning will be logged (and servlet listeners will actually not be registered).
    Error is not thrown to let writing more universal bundles with listener extensions (session related extensions will simply not work).
    If you want to throw exception in such case, use special option:
    ```java
    bundle.option(InstallersOptions.DenySessionListenersWithoutSession, true)
    ```

!!! tip 
    Use guicey `#!java @Order` annotation to order servlets registration.
    ```java
    @Order(10)
    @WebListener
    public class MyListener implements ServletContextListener {...}
    ```
       
### Admin context

By default, installer target application context. If you want to install into admin context then 
use guicey `@AdminContext` annotation.

For example: 

```java
@AdminContext
@WebListener
public class MyListener implements ServletContextListener {...}
```

Will install filter in admin context only.

If you want to install in both contexts use andMain attribute:

```java
@AdminContext(andMain = true)
@WebListener
public class MyListener implements ServletContextListener {...}
```
  