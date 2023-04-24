# Web servlet installer

!!! summary ""
    WebInstallersBundle / [WebServletInstaller](https://github.com/xvik/dropwizard-guicey/tree/dw-2.1/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/web/WebServletInstaller.java)        

Register new servlet in main or admin contexts.

## Recognition

Detects classes annotated with `@javax.servlet.annotation.WebServlet` annotation and register them in dropwizard environment.

```java
@WebServlet("/mapped")
public class MyServlet extends HttpServlet { ... }
```

Only the following annotation properties are supported: `name`, `urlPatterns` (or `value`), `initParams`, `asyncSupported`.

Servlet name is not required. If name not provided, it will be generated as:
. (dot) at the beginning to indicate generated name, followed by lower-cased class name. If class ends with "servlet" then it will be cut off.
For example, for class "MyCoolServlet" generated name will be ".mycool".

!!! warning
    One or more specified servlet url patterns may clash with already registered servlets. By default, such clashes are just logged as warnings.
    If you want to throw exception in this case, use special option:
    ```java
    bundle.option(InstallersOptions.DenyServletRegistrationWithClash, true)
    ```
    Note that clash detection relies on servlets registration order so clash may not appear on your servlet but on some other servlet manually registered later 
    (and so exception will not be thrown).

!!! tip 
    Use guicey `#!java @Order` annotation to order servlets registration.
    ```java
    @Order(10)
    @WebServlet("/mapped")
    public class MyServlet extends HttpServlet 
    ```
   
There is a difference between using servlet installer and registering servlets with guice servlet module:
guice servlet module handles registered servlets and filters internally in GuiceFilter (which is installed by guicey in both app and admin contexts).
As a side effect, there are some compatibility issues between guice servlets and native filters (rare and usually not blocking, but still).

Installer use guice only for servlet instance creation and register this instance directly in dropwizard environment (using annotation metadata).       

### Async

Example of async servlet definition:

```java
@WebServlet(urlPatterns = "/async", asyncSupported = true)
public class AsyncServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final AsyncContext context = req.startAsync();
        context.start(() -> {
            context.getResponse().getWriter().write("done!");
            context.complete();
        });
    }
}
```    
    
!!! note ""
    Note that guice servlet module does not allow using async servlets, so installer is the only option to install async servlets.
    
### Admin context

By default, installer target application context. If you want to install into admin context then 
use guicey `@AdminContext` annotation.

For example: 

```java
@AdminContext
@WebServlet("/mapped")
public class MyServlet extends HttpServlet { ... }
```

Will install servlet in admin context only.

If you want to install in both contexts use andMain attribute:

```java
@AdminContext(andMain = true)
@WebServlet("/mapped")
public class MyServlet extends HttpServlet { ... }
```
  