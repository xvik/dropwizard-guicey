

## Web Servlet
[WebServletInstaller](src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/web/WebServletInstaller.java)
finds classes annotated with `@WebServlet` annotation and register them. Support ordering.

```java
@WebServlet("/mapped")
public class MyServlet extneds HttpServlet { ... }
```

Only the following annotation properties are supported: name, urlPatterns (or value), initParams, asyncSupported 
([example async](src/test/groovy/ru/vyarus/dropwizard/guice/web/async/AsyncServletTest.groovy#L52)).

Servlet name is not required. If name not provided, it will be generated as:
. (dot) at the beginning to indicate generated name, followed by lower-cased class name. If class ends with "servlet" then it will be cut off.
For example, for class "MyCoolServlet" generated name will be ".mycool".

One or more specified servlet url patterns may clash with already registered servlets. By default, such clashes are just logged as warnings.
If you want to throw exception in this case, use `InstallersOptions.DenyServletRegistrationWithClash` option. 
Note that clash detection relies on servlets registration order so clash may not appear on your servlet but on some other servlet registered later 
(and so exception will not be thrown).

## Web Filter

[WebFilterInstaller](src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/web/WebFilterInstaller.java)
finds classes annotated with `@WebFilter` annotation and register them. Support ordering.

```java
@WebFilter("/some/*")
public class MyFilter implements Filter { ... }
```

Only the following annotation properties are supported: filterName, urlPatterns (or value), servletNames, dispatcherTypes, initParams, asyncSupported
([example async](src/test/groovy/ru/vyarus/dropwizard/guice/web/async/AsyncFilterTest.groovy#L47)).
Url patterns and servlet names can't be used at the same time.

Filter name is not required. If name not provided, then it will be generated as: 
. (dot) at the beginning to indicate generated name, followed by lower-cased class name. If class ends with "filter" then it will be cut off.
For example, for class "MyCoolFilter" generated name will be ".mycool".

## Web Listener

[WebListenerInstaller](src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/web/listener/WebListenerInstaller.java)
finds classes annotated with `@WebListener` annotation and register them. Support ordering.

Supported listeners (the same as declared in annotation):
 * javax.servlet.ServletContextListener
 * javax.servlet.ServletContextAttributeListener
 * javax.servlet.ServletRequestListener
 * javax.servlet.ServletRequestAttributeListener
 * javax.servlet.http.HttpSessionListener
 * javax.servlet.http.HttpSessionAttributeListener
 * javax.servlet.http.HttpSessionIdListener

```java
@WebListener
public class MyListener implements ServletContextListener, ServletRequestListener {...}
```

Listener could implement multiple listener interfaces and all types will be registered.

By default, dropwizard is not configured to support sessions. If you define session listeners without configured session support
then warning will be logged (and servlet listeners will actually not be registered).
Error is not thrown to let writing more universal bundles with listener extensions (session related extensions will simply not work).
If you want to throw exception in such case, use `InstallersOptions#DenySessionListenersWithoutSession` option.
