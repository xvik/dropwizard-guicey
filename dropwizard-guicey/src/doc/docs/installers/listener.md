# Web listener installer

!!! summary ""
    WebInstallersBundle / [WebListenerInstaller](https://github.com/xvik/dropwizard-guicey/tree/master/dropwizard-guicey/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/web/listener/WebListenerInstaller.java)

Register new web listener in main or admin contexts.

## Recognition

Detects classes annotated with `@jakarta.servlet.annotation.WebListener` and registers them in the Dropwizard environment.

```java
@WebListener
public class MyListener implements ServletContextListener, ServletRequestListener {...}
```

!!! note ""
    Listener could implement multiple listener interfaces and all types will be registered.

Supported listeners (the same as declared in the annotation):

 * jakarta.servlet.ServletContextListener
 * jakarta.servlet.ServletContextAttributeListener
 * jakarta.servlet.ServletRequestListener
 * jakarta.servlet.ServletRequestAttributeListener
 * jakarta.servlet.http.HttpSessionListener
 * jakarta.servlet.http.HttpSessionAttributeListener
 * jakarta.servlet.http.HttpSessionIdListener


!!! warning ""
    By default, Dropwizard is not configured to support sessions. If you define session listeners without configured session support
    then warning will be logged (and servlet listeners will actually not be registered).
    Error is not thrown to let writing more universal bundles with listener extensions (session related extensions will simply not work).
    If you want to throw exception in such case, use special option:
    ```java
    bundle.option(InstallersOptions.DenySessionListenersWithoutSession, true)
    ```

!!! tip
    Use the Guicey `#!java @Order` annotation to order listener registration.
    ```java
    @Order(10)
    @WebListener
    public class MyListener implements ServletContextListener {...}
    ```

### Admin context

By default, the installer targets the application context. If you want to install it into the admin context, then
use Guicey `@AdminContext` annotation.

For example:

```java
@AdminContext
@WebListener
public class MyListener implements ServletContextListener {...}
```

Will install the listener in the admin context only.

If you want to install in both contexts, use the `andMain` attribute:

```java
@AdminContext(andMain = true)
@WebListener
public class MyListener implements ServletContextListener {...}
```
