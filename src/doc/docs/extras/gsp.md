# Guicey Server Pages

!!! summary ""
    [Extensions project](https://github.com/xvik/dropwizard-guicey-ext/tree/master/guicey-server-pages) module

Brings the simplicity of JSP to dropwizard-views. 
Basement for pluggable and extendable ui applications (like dashboards).

!!! warning ""
    **EXPERIMENTAL MODULE** 

Features:

* Use standard dropwizard modules: [dropwizard-views](https://www.dropwizard.io/en/release-2.0.x/manual/views.html) and [dropwizard-assets](https://www.dropwizard.io/en/release-2.0.x/manual/core.html#serving-assets)
* Support direct templates rendering (without rest resource declaration) 
* Static resources, direct templates and dropwizard-views rest endpoints are handled under the same url
(like everything is stored in the same directory - easy to link css, js and other resources)
* Multiple ui applications declaration with individual errors handling (error pages declaration like in servlet api, but not global)
* Ability to extend applications (much like good old resources copying above exploded war in tomcat)

## Problem

Suppose you want to serve your ui to from the root url, then you need to re-map rest:

```yaml
server:
  rootPath: '/rest/*'
  applicationContextPath: /
```

Static resources are in classpath:

```
com/something/
    index.html
    style.css
``` 

Using dropwizard assets bundle to configure application:

```java
bootstrap.addBundle(new AssetsBundle("/com/something/", "/", "index.html"));
```

Note that `index.html` could reference css with relative path:

```html
<link href="style.css" rel="stylesheet">
```

Now if we want to use template instead of pure html we configure dropwizard views:

```java
bootstrap.addBundle(new ViewBundle<MyConfiguration>());
``` 

Renaming `index.html` to `index.ftl` and add view resource:

```java
@Path("/ui/")
@Produces(MediaType.TEXT_HTML)
public class IndexResource {
    
    public static class IndexView extends View {
        public IndexView() {
            super("/com/something/index.ftl");
        } 
    }

    @GET
    public IndexView get() {
        return new IndexView();
    }
}
```

As a result, index page url become `/rest/ui/` so we need to link css resource with full path (`/style.css`) instead of relative
(or even re-configure server to back rest mapping to into root).

It is already obvious that asset servlet and templates are not play well together.

### Solution  

The solution is obvious: make assets servlet as major resources supplier and with an additional filter to
detect template requests and redirect rendering to actual rest.

So example above should become:

```
com/something/
    index.ftl
    style.css
``` 

Where `index.ftl` could use

```html
<link href="style.css" rel="stylesheet">
```

because it is queried by url `/index.ftl`: no difference with usual `index.html` - template rendering is hidden
(and direct template file even don't need custom resource). 

When we need custom resource (most likely, for parameters mapping) we can still use it:

```java
@Path("/views/ui/")
@Template("foo.ftl")
@Produces(MediaType.TEXT_HTML)
public class IndexResource {
    
    @GET
    @Path("/foo/{id}")
    public IndexView get(@PathParam("id") String id) {
        return new TemplateView();
    }
}
```   

It would be accessible from assets root `/foo/12` (more on naming and mapping details below).
Under the hood `/foo/12` will be recognized as template call and redirected (server redirect) to `/rest/ui/foo/12`. 

As you can see rest endpoints and templates are now "a part" of static resources.. just like good-old 
JSP (powered with rest mappings). And it is still pure dropwizard views.

GSP implements per-application error pages support so each application could use it's own errors. In pure 
dropwizard-views such things should be implemented manually, which is not good for application incapsulation.

## Setup 

[![JCenter](https://img.shields.io/bintray/v/vyarus/xvik/dropwizard-guicey-ext.svg?label=jcenter)](https://bintray.com/vyarus/xvik/dropwizard-guicey-ext/_latestVersion)
[![Maven Central](https://img.shields.io/maven-central/v/ru.vyarus.guicey/guicey-server-pages.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/ru.vyarus.guicey/guicey-gsp)

Avoid version in dependency declaration below if you use [extensions BOM](../guicey-bom). 

Maven:

```xml
<dependency>
  <groupId>ru.vyarus.guicey</groupId>
  <artifactId>guicey-server-pages</artifactId>
  <version>5.0.0-0</version>
</dependency>
```

Gradle:

```groovy
compile 'ru.vyarus.guicey:guicey-server-pages:5.0.0-0'
```

See the most recent version in the badge above.

## Usage

First of all, global GSP bundle must be installed in main application class. It 
configures and installs dropwizard-views (global). It supports the same configurations as
pure dropwizard-views bundle.

```java
GuiceBundle.builder()
    .bundles(ServerPagesBundle.builder().build());
```

!!! warning ""
    Remove direct dropwizard-views bundle registrations (`ViewBundle`) if it was already used in application.  

### Template engines

Out of the box [dropwizard provides](https://www.dropwizard.io/en/release-2.0.x/manual/views.html) `freemarker` and `mustache` engines support. 
You will need to add dependency to one of them (or both) in order to activate it (or, maybe, some third party engine):

* compile (`io.dropwizard:dropwizard-views-freemarker`) 
* compile (`io.dropwizard:dropwizard-views-mustache`)

Other template engines available as 3rd party modules. If your template engine is not yet supported
then simply implement `io.dropwizard.views.ViewRenderer` in order to support it.

`ViewRenderer` implementations are loaded automatically using ServiceLoader mechanism. 
If your renderer is not declared as service then simply add it directly:

```java
.bundles(ServerPagesBundle.builder()
        .addViewRenderers(new MyTempateSupport())
        .build());
```

Duplicate renderers are automatically removed.

List of detected template engines will be printed to console. You can get list of used renderers
from bundle instance `ServerPagesBundle#getRenderers()`  

!!! note  
    This is pure dropwizard-views staff (everything is totally standard).

### Configuration

Views yaml configuration binding is the same as in dropwizard-views.

```yaml
views:
  freemarker:
    strict_syntax: true
  mustache:
    cache: false
```

Where `freemarker` and `mustache` are keys from installed template renderer 
`io.dropwizard.views.ViewRenderer#getConfigurationKey()`. 

```java
public class AppConfig extends Configuration {
    @JsonProperty
    private Map<String, Map<String, String>> views;
    
    public Map<String, Map<String, String>> getViews() { return views;} 
}
```

```java
.bundles(ServerPagesBundle.builder()
        .viewsConfiguration(AppConfig::getViews)
        .build());
```

If `AppConfig#getViews` return `null` then empty map will be used instead as config.

Additionally, to direct yaml configuration binding, you can apply exact template engine modifications

```java
.bundles(ServerPagesBundle.builder()
        .viewsConfiguration(AppConfig::getViews)
        .viewsConfigurationModifier("freemarker", 
                map -> map.put("cache_storage", "freemarker.cache.NullCacheStorage"))
        .build());
``` 
   
Modifier always receive not null map (empty map is created automatically in global configuration).

Multiple modifiers could be applied (even for the same section). Each GSP application could also apply modifiers
(this is useful to tune defaults: e.g. in case of freemarker, application may need to apply default imports).

The final configuration (after all modifiers) could be printed to console with `.printViewsConfiguration()`.
Also, configuration is accessible from the bundle instance: `ServerPagesBundle#getViewsConfig()`.

## Applications

Each GSP application is registered as separate bundle in main or admin context:

```java
.bundles(ServerPagesBundle.app("projectName-ui", "com.app.ui", "/")
                    .indexPage("index.ftl")
                    .build())
                    
.bundles(ServerPagesBundle.adminApp("projectName-admin", "com.app.admin", "/admin")
                    .build())                    
```   

Unlimited number of applications may be registered on each context.


```java
app("projectName-ui", "com.app.ui", "/")
```

* `projectName-ui` - unique(!) application name. Uniqueness is very important as name used for rest paths.
    To avoid collisions it's recommended to use domain-prefixed names to better identify application related resources. 
* `com.app.ui` - classpath package with resources (application "root" folder; the same meaning as in dropwizard-assets);
    Also, it may be configured as `/com/app/ui/`, but package notion is easier to understand 
* `/` - application mapping url (in main or admin context; the same as in dropwizard-assets)
    (if context is prefixed (`server.applicationContextPath: /some` or `server.adminContextPath: /admin`) then GSP 
    application will be available under this prefix)

!!! warning 
    It is a common desire to map ui on main context's root path (`/`), but, by default, dropwizard
    maps rest there and so you may see an error:

```
java.lang.IllegalStateException: Multiple servlets map to path /*: app[mapped:JAVAX_API:null],io.dropwizard.jersey.setup.JerseyServletContainer-1280682[mapped:EMBEDDED:null]
```

In this case simply re-map rest in yaml config:
```yaml
server:
  rootPath: '/rest/*'
```

If application requires resources from multiple paths, use:

```java
ServerPagesBundle.app("projectName-ui", "com.app.path1", "/")
    .attachAssets("com.app.path1")
    ...
```    

For example, this can be useful to attach some shared resources.
To attach webjars there is a [pre-defined shortcut](#webjars-usage).

You can even attach resources path for exact sub url:

```java
ServerPagesBundle.app("projectName-ui", "com.app.path1", "/")
    .attachAssets("/sub/path/", "com.app.path.sub")
    ...
``` 

And for urls starting from `/sub/path/` application will look static resources
(and templates) inside `/com/app/path/sub/` first, and only after that under root paths. 

This way, you can map resources from different packages as you want. This is like
if you copied everything from different packages into one place (like exploded war).

### Template engine constraint

As GSP application declaration is separated from views configuration (GSP application
may be even a 3rd party bundle) then it must be able to check required template engines presence.

For example, this application requires freemarker:

```java
.bundles(ServerPagesBundle.app("projectName-ui", "com.app.ui", "/")
                    .requireRenderers("freemarker")
                    .build())
```

Template engine name is declared in `io.dropwizard.views.ViewRenderer#getConfigurationKey()` (same name used in configuration).   

### Templates support

As dropwizard-views is used under the hood, all templates are always rendered with
rest endpoints. All these rest endpoints are part of global rest.

It is recommended to start all view rest with `/view/` to make it clearly distinguishable
from application rest. Also, rest views, related to one GSP application must also start
with a common prefix: for example, `/view/projectName/ui/..`.

You need to map required rest prefix in GSP application:

```java
.bundles(ServerPagesBundle.app("projectName-ui", "com.app.ui", "/")
                    .mapViews("/view/projectName/ui/")
```                   

This will "map" all view rest paths after prefix directly to GSP application root.
So if you have view resource `/view/projectName/ui/page1/action` you can access it
relatively to application mapping root ("/" in the example above) as `/page1/action`.

By default, if views mapping is not declared manually, it would be set to application name
(`/...` -> `/projectName-ui/...`)

Under startup dropwizard logs all registered rest enpoints, so you can always see original
rest mapping paths. For each registered GSP application list of "visible" paths will be logged as: 

```
INFO  [2019-06-07 04:10:47,978] io.dropwizard.jersey.DropwizardResourceConfig: The following paths were found for the configured resources:

    GET     /rest/views/projectName/ui/sample (com.project.ui.SampleViewResource)
    POST    /rest/views/projectName/ui/other (com.project.ui.SampleViewResource)

INFO  [2019-06-07 04:10:47,982] ru.vyarus.guicey.gsp.app.ServerPagesApp: Server pages app 'com.project.ui' registered on uri '/*' in main context

    Static resources locations:
        com.app.ui

    Mapped handlers:
        GET     /sample  (com.project.ui.SampleViewResource #sample)
        POST    /other  (com.project.ui.SampleViewResource #other)
```

Here you can see real rest mapping `GET     /rest/views/projectName/ui/sample` and
how it could be used relative to application path `GET     /sample`. 

This report will always contain all correct view paths which must simplify overall understanding:
if path not appear in the report - it's incorrectly mapped and when it's appear - always use the path
from application report to access it.

But that's not all: you can actually map other rest prefixed to sub urls:

```java
.bundles(ServerPagesBundle.app("projectName-ui", "com.app.ui", "/")
                    .mapViews("/sub/path/", "/view/projectName2/ui/something/")
```

This way, it is possible to combine rest endpoints, written for different applications
(or simply prerare common view resource groups). Just note that in contrast to resources
mapping, only one prefix may be mapped on each url!

You will also need to map static resources location accordingly if you use relative template paths.

#### Direct templates

You can also render template files without declaring view rest at all (good old jsp way).

If we call supported template type directly like `http://localhost:8080/template.ftl` it will be recognized
as direct template call and rendered. Template file must be placed under registered classpath path root:
`/com/app/ui/template.ftl`.

Templates in sub folders will be rendered the same way, e.g. `http://localhost:8080/sub/path/template.ftl`
will render `/com/app/ui/sub/path/template.ftl`. 

### Template rest declaration

Declaration differences with pure dropwizard-views:

* `@Path` value must start with mapped prefix (see the chapter above) 
* Resource class must be annotated with `@Template` (even without exact template declaration)
* `TemplateView` must be used instead of dropwizard `View` as a base class for view models.

Suppose we declaring page for gsp application `.app("projectName-ui", "com.app.ui", "/")`

As in pure views, in most cases we will need custom model object:

```java
public class SampleView extends TemplateView {
    private String name;
    
    public SampleView(String name) {
        this.name = name;
    }
    
    public String getName() { return this.name; } 
}
```

!!! note 
    Custom model is optional - you can use `TemplateView` directly, as default "empty" model.

```java
@Path("/views/projectName/ui/sample/")
@Template("sample.ftl")
public class SamplePage {

    @Path("{name}")
    public SampleView doSomething(@PathParam("name") String name) {
        return new SampleView(name);        
    }    
}
``` 

And example template:

```ftl
<#-- @ftlvariable name="" type="com.project.ui.SampleView" -->
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Sample page</title>
    <link rel="stylesheet" type="text/css" href="style.css">
</head>
<body>
    Name: ${name}
</body>
</html>
```

After application startup, new url must appear in GSP application console report.

If we call new page with `http://localhost:8080/sample/fred` we should see
`Name: fred` as a result.

!!! note 
    Can pure dropwizard-views resources be used like that? Actually, yes, but
    they must be annotated with `@Template` because not annotated resources are not 
    considered as potential GSP application views (and will not be shown in console report).

#### @Template

`@Template` annotation must be used on ALL template resources. It may declare default
template for all methods in resource (`@Template("sample.ftl")`) or be just a marker annotation (`@Template`).

Annotation differentiate template resources from other api resources and lets you delare jersey
extension only for template resources:

```java
@Provider
@Template
public class MyExtensions implements ContainerRequestFilter {
    ...
} 
``` 

This request filter will be applied only to template resources. Such targeting is used 
internally in order to not affect global api with GSP specific handling logic.

Template path resolution:

* If path starts with `/` then it would be resolved from classpath root
* Resolution relative to resource class
* Resolution relative to static resources location (`/com/app/ui/` in the example above) 

Examples: 

* `@Template("/com/project/custom/path/sample.ftl")` - absolute declaration.
* `@Template("sub/sample.ftl")` - relative declaration
* `@Template("../sub/sample.ftl")` - relative declaration

Even if template is configured in the annotation, exact resource method could specify it's own 
template directly in `TemplateView` constructor:

```java
@Path("/views/projectName/ui/sample/")
@Template("sample.ftl")  // default template
public class SamplePage {

    @Path("/")
    public TemplateView doSomething() {
        // override template
        return new TemplateView("otherTemplate.ftl");        
    }    
}
```

Template path resolution rules are the same as with annotation.

#### TemplateContext

`TemplateContext` contains all template contextual information. It could be accessed inside template
with model's `getContext()`, e.g.:

```ftl
<#-- @ftlvariable name="" type="com.project.ui.SampleView" -->
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Sample page</title>
    <link rel="stylesheet" type="text/css" href="style.css">
</head>
<body>
    Current url: ${context.url}
</body>
</html>
```

In rest view resources it could be accessed with a static lookup: `TemplateContext.getInstance()`.

This way you can always know current gsp application name, original url (before redirection to rest),
root application mapping prefix and get original request object (which may be required for error pages).

### Index page

Index page is a page shown for root application url (`/`). It could be declared as:

```java
.bundles(ServerPagesBundle.app("com.project.ui", "/com/app/ui/", "/")
                    .indexPage('index.html')
                    .build())
```

It could be:
* Direct resource: `index.html`
* Direct template: `index.ftl`
* Rest powered template: `/mapping/` 

!!! note
    By default, index page is set to `""` because most likely your index page will be 
    handled with rest and `""` will redirect to root path (for current application): `/com.project.ui/` 

### Error pages

Each GSP application could declare its own error pages (very similar to servlet api). 

It could be one global error page and different pages per status:

```java
.bundles(ServerPagesBundle.app("com.project.ui", "/com/app/ui/", "/")
                    .errorPage('error.html')
                    .errorPage(500, '500.html')
                    .errorPage(401, '401.html')
                    .build())
```

As with index pages, error page may be direct static html, direct template or rest path.

!!! important
    Error pages are shown ONLY if requested type was `text/html` and pass error as is 
    in other cases. Simply because it is not correct to return html when client was expecting different type. 

Errors handling logic detects:

1. Static resources errors (404)
2. Exceptions during view resource processing (including rendering errors)
3. Direct error codes return without exception (`Resounce.status(404).build()`) 

Error pages may use special view class (or extend it) `ErrorTemplateView` which
collects additional error-related info.

For example, even direct error template could show:

```ftl
<#-- @ftlvariable name="" type="ru.vyarus.guicey.gsp.views.template.ErrorTemplateView" -->

<h3>Url ${erroredUrl} failed to render with ${errorCode} status code</h3>
<div>Error: ${error.class.simpleName}</div>
<pre>
  ${errorTrace}
</pre>
```

For rest-powered error page:

```java
@Path("/views/projectName/ui/error/")
@Template("error.ftl")
public class ErrorPage {

    @Path("/")
    public TemplateView render() {
        // it may be any extending class if additional properties are required (the same as usual)
        ErrorTemplateView view = new ErrorTemplateView();
        WebApplicationException ex = view.getError();
        // analyze error
        return view;
    }        
}
```

(this error page can be mapped as `.errorPage("/error/")`).

`view.getError()` always return `WebApplicationException` so use `ex.geCause()` to get original exception.
But there will not always be useful exception because direct exception is only one of error cases (see above).

In order to differentiate useful exceptions, you can check:

```java
if (ex instanceof TracelessException) {
    // only status code availbale
    int status = ((TracelessException) ex).getStatus();
} else {
    // actually throwed exception to analyze
    Throwable actualCause = ex.getCause()
}
```

`TracelessException` may be either `AssertError` for static resource fail or `TemplateRestCodeError`
for direct non 200 response code in rest.

!!! important 
    GSP errors handling override [ExceptionMapper](https://www.dropwizard.io/en/release-2.0.x/manual/views.html#template-errors)
    and [views errors](https://www.dropwizard.io/en/release-2.0.x/manual/views.html#custom-error-pages)
    mechanisms because it intercept exceptions before them (using `RequestEventListener`)! So your 
    `ExceptionMapper` will be called, but user will still see GSP error page. 

The motivation is simple: otherwise it would be very hard to write side effect free GSP applications
because template resources exceptions could be intercepted with `ExceptionMapper`'s declared
in dropwizard application.  

To overcome this limitation, you can disable errors handling with `@ManuaErrorHandling`.
It may be applied on resource method or to resource class (to disable on all methods).

For example:

```java
@Path("/com.project.ui/error/")
@Template("page.ftl")
public class ErrorPage {

    @ManualErrorHandling
    @Path("/")
    public TemplateView render() {
        // if exception appear inside this method, it would be handled with ExceptionMapper
        // GSP error page will not be used
        
        // Also, if method return non 200 error code (>=400) like 
        // return Response.status(500).build()
        // it would be also not handled with GSP error mechanism (only pure dropwizard staff) 
    }        
}
```                                        

!!! note ""
    Note that disabled errors will be indicated as `[DISABLED ERRORS]` in console report. 

### SPA routing

If you use Single Page Applications then you may face the need to recognize html5 client routing urls
and redirect to index page. You can read more about it in [guicey SPA module](../guicey-spa).

As guicey SPA module can't be used directly with GSP, it's abilities is integrated directly and could 
be activated with:

```java
.bundles(ServerPagesBundle.app("projectName-ui", "com.app.ui", "/")
                    .spaRouting()
                    .build())
```

Or, if custom detection regex is required: `.spaRouting(customRegex)`

Most likely, usage use-case would be: index page requires some server-size templating.

### Template requests detection

GSP must differentiate static resource calls from template calls. It assumes that static
resources would always end with an extension (e.g. `/something/some.ext`) and so:

1. If request without extension - it's a template
2. If extension is recognized as template extension - render as template
3. Other cases are static resources

The following regular expression used for extension detection:
```regexp
(?:^|/)([^/]+\.(?:[a-zA-Z\d]+))(?:\?.+)?$
```

If it does not cover you specific cases, it could be changed using:

```java
.bundles(ServerPagesBundle.app("com.project.ui", "/com/app/ui/", "/")
                    .filePattern("(?:^|/)([^/]+\\.(?:[a-zA-Z\\d]+))(?:\\?.+)?$")
                    .build())
```

In case when you have static files without extension, you can include them directly 
into detection regexp (using regex or (|) syntax).

Pattern must return detected file name as first matched group (so direct template could be detected).
Pattern is searched (find) inside path, not matched (so simple patterns will also work).

### Extending applications

In "war world" there is a a very handy thing as overlays: when we can apply our resources
"above" existing war. This way we can replace existing files (hack & tune) and add our own files
so they would live inside app as they were always be there.

In order to achieve similar goals there is a application extension mechanism. 

For example we application:

```java
.bundles(ServerPagesBundle.app("projectName-ui", "com.app.ui", "/")
                    .build())
```

With multiple pages inside:

```
/com/app/ui/
    page1.ftl
    page2.ftl
    style.css
```

Each page could include style relatively as `style.css`. Most likely, there will even
be master template (freemarker) which unifies styles and common script installation.

This application is distributed as 3rd party bundle (jar). If we need to add one more page 
to this application in our current dropwizard application, we can:

```java
.bundles(ServerPagesBundle.extendApp("projectName-ui")
        .attachAssets("com.otherApp.ui.ext")
        .build())
```

And put another page into classpath:

```
/com/otherApp/ui/ext/
    page3.ftl    
```

This page could also reference `style.css` relatively, the same as pages in the main application.

On application startup, you will notice new resources location:

```
    Static resources locations:
        /com/app/ui/
        /com/otherApp/ui/ext/
```

Now both locations are "roots" for the application. The same way as if we copied
`/com/otherApp/ui/ext/` into `/com/app/ui/`.

`http://localhost:8080/page3.ftl` would correctly render new page.

There may be unlimited number of application extensions. If extended application
is not available, it is not considered as an error: it's assumed as optional
application extension, which will be activated if some 3rd party jar with GSP application  
appear in classpath.

You can also map addition rest prefixes:

```java
.bundles(ServerPagesBundle.extendApp("projectName-ui")
        .mapViews("/sub/folder/", "/views/something/ext/")
        .build())
```        

In some cases, extensions may depend on dropwizard configuration, but
bundles created under initialization phase. To workaround this you can 
use delayed extensions init:

```java
.bundles(ServerPagesBundle.extendApp("projectName-ui")
        .delayedConfiguration((env, assets, views) -> {
            if (env.configuration().isExtensionsEnabled()) {
                assets.attach("com.foo.bar")
            }           
         })
        .build())
```

### Webjars usage

If you want to use resources from [webjars](https://www.webjars.org/) in GSP application:

```java
.bundles(ServerPagesBundle.app("com.project.ui", "/com/app/ui/", "/")
                    .attachWebjars()
                    .build())
```

For example, to add jquery:

```groovy
compile 'org.webjars.npm:jquery:3.4.1'
```

And it could be referenced as:

```html
<script src="jquery/3.4.1/dist/jquery.min.js"/>
```


Under the hood `.attachWebjars()` use extensions mechanism and adds 
`META-INF/resources/webjars/` as application resources path:

```java
ServerPagesBundle.app("com.project.ui", "/com/app/ui/", "/")
    ...
    .attachAssets("META-INF/resources/webjars/")
```

OR

```java
.bundles(ServerPagesBundle.extendApp("app name")
    .attachAssets("META-INF/resources/webjars/")
    .build())
```

!!! tip
    You can always see the content of webjar on [webjars site](https://www.webjars.org/) by clicking
    on package "Files" column. Use everything after "META-INF/resources/webjars/" to reference file.