### GSP SPA HTML5 routing sample

This is an evolution of [SPA example](../ext-spa) but with dynamic base path.

Use [GSP guicey module]((https://github.com/xvik/dropwizard-guicey/tree/dw-3/guicey-server-pages))
for index page to set correct base path. Note that gsp module extends spa module and so inherits its features.

There are 3 examples:
1. Same as in SPA example:
    * Run application
    * Try http://localhost:8080/app -> index page will open
    * Switch page (e.g. to /foo)
    * Refresh browser -> index page must be loaded for route url (http://localhost:8080/app/foo)

    The difference is that index page is freemarker template and base path mapped dynamically:
    ```html
   <base href="${context.rootUrl}"/>
   ``` 

2. Same static resources mapped on different url: `http://localhost:8080/app2`

3. The same application, but index page mapped as rest view, so you can use custom computed properties in 
    the template. Note that index page mapped to rest path:
    ```java
     ServerPagesBundle.app("app3", "/app/", "/app3/")
                // index page is now freemarker template
                .mapViews("/views/app3/") // <-- map rest views for application
                .indexPage("/index/")  // <-- rest view path, not actual page
    ```
   Template file is delcared in view class:
   ```java
   @Template("index3.ftl")
   public class ComplexIndexView {
   ```
   Try `http://localhost:8080/app3`: it would be the same index page with an additional line:
   `<p>Dynamic value: ${sample}</p>`
   
NOTE: all apps need to explicitly enable SPA routing with `.spaRouting()`   