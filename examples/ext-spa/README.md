### SPA HTML5 routing sample

Use [SPA guicey module]((https://github.com/xvik/dropwizard-guicey/tree/dw-3/guicey-spa)).

Sample view application is in src/main/resources/app. Application use VuewRouter with two routes
: /foo and /bar.

Application installed on /app context:

```java
SpaBundle.app("app", "/app", "/app/")
```

(Application named "app" with resources located at "/app" on uri "/app/")

NOTE that index.html sets base tag:

```html
<base href="/app/"/>
```

* Run application
* Try http://localhost:8080/app -> index page will open
* Switch page (e.g. to /foo)
* Refresh browser -> index page must be loaded for route url (http://localhost:8080/app/foo)