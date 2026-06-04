# Application URLs builder

Sometimes, it is required to build an application URL: for example, generate an email link, or use it for a redirect.
But server configuration can change, affecting the target URL:

* Default or simple server (difference in admin port)
* Port
* Contexts:
  - server.applicationContextPath - application context
  - server.rootPath - rest context
  - server.adminContextPath - admin context

There is an `AppUrlBuilder` class for building links with automatic resolution of
the current server configuration (the same as `ClientSupport`).

Also, the mechanism used in [resource clients](test/general/client.md#resource-clients)
can also be used to build application URLs in a type-safe manner (for REST methods).

`AppUrlBuilder` is not bound by default in the Guice context, but can be injected (as a JIT binding):

```java
@Inject AppUrlBuilder builder;
```

Or it could be created manually:

```java
AppUrlBuilder builder = new AppUrlBuilder(environment);
```

There are 3 scenarios:

* Localhost URLs: the default mode when all URLs contain "localhost" and the application port.
* Custom host: `builder.forHost("myhost.com")` when a custom host is used instead of localhost and the application port
  is applied automatically
* Proxy mode: `builder.forProxy("https://myhost.com")` when the application is behind some proxy
  (like Apache or Nginx) hiding its real port.

Examples:

```java
// http://localhost:8080/
builder.root("/")
// http://localhost:8080/
builder.app("/")
// http://localhost:8081/
builder.admin("/")
// http://localhost:8080/
builder.rest("/")

// http://localhost:8080/users/123
builde.rest(Resource.class).method(r -> r.get(123)).build()
// http://localhost:8080/users/123
builde.rest(Resource.class).method(r -> r.get(null)).pathParam("id", 123).build()


// https://some.com:8081/something
builder.forHost("https://some.com").admin("/something")

// https://some.com/something
builder.forProxy("https://some.com").admin("/something")
```

`AppUrlBuilder` can be used to access the current server configuration:

```java
// 8080
builder.getAppPort();
// 8081
builder.getAdminPort();
// "/" (server.adminContextPath)
builder.getAdminContextPath();
// "/" (server.applicationContextPath)
builder.getAppContextPath();
// "/" (server.rootPath)
builder.getRestContextPath();
```