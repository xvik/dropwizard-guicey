# Single page applications support

Provides a replacement for [dropwizard-assets](https://www.dropwizard.io/en/release-5.0.x/manual/core.html#serving-assets)
bundle for single page applications (SPA) to properly
handle HTML5 client routing.

Features:

* Pure Dropwizard bundle, but can be used with Guicey bundles
* Built on top of the `dropwizard-assets` servlet
* Support registration on main and admin contexts
* Multiple apps could be registered
* Sets no-cache headers for index page
* Regex could be used to tune routes detection

## Problem

The problem with SPA is HTML5 routing. For example, suppose your app base URL is `/app`
and the client route URL is `/app/someroute` (before, there was no problem because the route would
look like `/app/#!/someroute`). When a user hits refresh (or a bookmark) for such a route, the server is actually
called with the route URL. The server must recognize it and return the index page.

For example, the Angular 2 router uses HTML5 mode by default.

### Solution

The problem consists of two points:

1. Correctly process resource calls (css, js, images, etc) and return 404 for missed resources
2. Recognize application routes and return the index page instead

The bundle registers the `dropwizard-assets` servlet with a special filter above it. Filter tries to process
all incoming URLs. This approach guarantees that all calls to resources will be processed and
the index page will not be returned instead of a resource (solves problem 1).

If a resource is not found, the index page is returned. To avoid redirection in case of bad resources request,
filter will redirect only requests accepting 'text/html'. Additional regexp (configurable)
is used to recognize most resource calls and avoid redirection (showing the correct 404).

From example above, `/app/someroute` will return index page and `/app/css/some.css` will return css.
`/app/css/unknown.css` will return 404 because the resource call will be recognized and the CSS file does not exist.

## Setup

Maven:

```xml
<dependency>
  <groupId>ru.vyarus.guicey</groupId>
  <artifactId>guicey-spa</artifactId>
  <version>{{ gradle.version }}</version>
</dependency>
```

Gradle:

```groovy
implementation 'ru.vyarus.guicey:guicey-spa:{{ gradle.version }}'
```

Omit the version if the Guicey BOM is used.

## Usage

Register bundle:

```java
GuiceBundle.builder()
    .bundles(SpaBundle.app("app", "/app", "/").build());
```

This will register an app with the name "app" (name is used to name servlets and filters and must be unique).
Application files are located in "app" package in classpath (e.g. resources inside jar).
The application is mapped to the root context (note that this will work only if rest is mapped
to some sub context: e.g. with `server.rootPath: '/rest/*'` configuration).

```text
http://localhost:8080/ -> app index
http://localhost:8080/css/app.css -> application resource, located at /app/css/app.css in the classpath
http://localhost:8080/someroute -> application client route - index page returned
```

Example registration to admin context:

```java
.bundles(SpaBundle.adminApp("admin", "/com/mycompany/adminapp/", "/manager").build());
```

Register the "admin" application with resources in the "/com/mycompany/adminapp/" package, served from the "manager"
admin context (note that admin root is already used by Dropwizard admin servlet).

!!! tip
    Resources location can be declared both as path (`/com/mycompany/adminapp/`) or as package (`com.mycompany.adminapp`).

```text
http://localhost:8081/manager -> admin app index
```

You can register as many apps as you like. They just must use different URLs and have different names:

```java
.bundles(SpaBundle.app("app", "/app", "/").build(),
         SpaBundle.app("app2", "/app2", "/").build(),
         SpaBundle.adminApp("admin", "/com/mycompany/adminapp/", "/manager").build(),
         SpaBundle.adminApp("admin2", "/com/mycompany/adminapp2/", "/manager2").build());
```

!!! note
    If you publish SPA application not in the root path, don't forget to set appropriate `<base href="/path/"/>` tag.
    All modern client side routers rely on it. Pay attention that path in base tag must end with `/`.

### Index page

By default, the index page is assumed to be "index.html". It can be changed with:

```java
.bundles(SpaBundle.app("app", "/app", "/").indexPage("main.html").build());
```

### Prevent redirect regex

By default, the following regex is used to prevent resource redirection (to not send index for missed resource):

```regexp
\.(html|css|js|png|jpg|jpeg|gif|ico|xml|rss|txt|eot|svg|ttf|woff|woff2|cur)(\?((r|v|rel|rev)=[\-\.\w]*)?)?$
```

Could be changed with:

```java
.bundles(SpaBundle.app("app", "/app", "/")
        .preventRedirectRegex("\\.\\w{2,5}(\\?.*)?$")
        .build());
```

This regexp implements the naive assumption that all app routes do not contain an "extension".

Note: regexp is applied with `find` so use `^` or `$` to apply boundaries.
