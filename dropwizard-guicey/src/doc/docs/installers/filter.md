# Web filter installer

!!! summary ""
    WebInstallersBundle / [WebFilterInstaller](https://github.com/xvik/dropwizard-guicey/tree/master/dropwizard-guicey/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/web/WebFilterInstaller.java)

Register new filter in main or admin contexts.

## Recognition

Detects classes annotated with `@jakarta.servlet.annotation.WebFilter` and registers them in the Dropwizard environment.

```java
@WebFilter("/some/*")
public class MyFilter implements Filter { ... }
```

Only the following annotation properties are supported: `filterName`, `urlPatterns` (or `value`), `servletNames`, `dispatcherTypes`, `initParams`, `asyncSupported`.

!!! warning
    Url patterns and servlet names can't be used at the same time.

A filter name is not required. If no name is provided, then it will be generated as:
. (dot) at the beginning to indicate generated name, followed by lower-cased class name. If class ends with "filter" then it will be cut off.
For example, for class "MyCoolFilter" generated name will be ".mycool".

!!! tip
    Use the Guicey `#!java @Order` annotation to order filter registration.
    ```java
    @Order(10)
    @WebFilter("/some/*")
    public class MyFilter implements Filter { ... }
    ```

There is a difference between using filter installer and registering filters with guice servlet module:
guice servlet module handles registered servlets and filters internally in GuiceFilter (which is installed by Guicey in both app and admin contexts).
As a side effect, there are some compatibility issues between guice servlets and native filters (rare and usually not blocking, but still).

Installer use guice only for filter instance creation and register this instance directly in Dropwizard environment (using annotation metadata).

### Async

Example of async filter definition:

```java
@WebFilter(urlPatterns = "/asyncfilter", asyncSupported = true)
public class AsyncFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        final AsyncContext context = request.startAsync();
        context.start(() -> {
            context.getResponse().writer.write("done!");
            context.complete();
        });
    }

    @Override
    public void destroy() {
    }
}
```

!!! note ""
    Note that guice servlet module does not allow using async filters, so installer is the only option to install async filters.

### Admin context

By default, the installer targets the application context. If you want to install it into the admin context, then
use Guicey `@AdminContext` annotation.

For example:

```java
@AdminContext
@WebFilter("/some/*")
public class MyFilter implements Filter { ... }
```

Will install filter in admin context only.

If you want to install in both contexts, use the `andMain` attribute:

```java
@AdminContext(andMain = true)
@WebFilter("/some/*")
public class MyFilter implements Filter { ... }
```
