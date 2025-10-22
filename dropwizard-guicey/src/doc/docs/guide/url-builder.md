# Application URLs builder

Sometimes, it is required to build application url: for example, generate email link, or use it for redirect.
But server configuration could be changed, affecting target url:

* Default or simple server (difference for admin port)
* Port
* Contexts:
  - server.applicationContextPath - application context
  - server.rootPath - rest context
  - server.adminContextPath - admin context
    
There is a `AppUrlBuilder` class for building links with automatic resolution of 
the current server configuration (the same as `ClientSupport`).

Also, mechanism, used in [resource clients](test/general/client.md#resource-clients),
could be also used to build application urls in a type-safe manner (for rest methods).

`AppUrlBuilder` is not bound by default in the guice context, but could be injected (as jit binding):

```java
@Inject AppUrlBuilder builder;
```

Or it could be created manually:

```java
AppUrlBuilder builder = new AppUrlBuilder(environment);
```

There are 3 scenarios:

* Localhost urls: the default mode when all urls contain "localhost" and application port.
* Custom host: `builder.forHost("myhost.com")` when custom host used instead of localhost and application port
  is applied automatically
* Proxy mode: `builder.forProxy("https://myhost.com")` when application is behind some proxy
  (like apache or nginx) hiding its real port.

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

`AppUrlBuilder` could be used to get access to current server configuration:

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