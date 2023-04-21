### Guicey server pages example

Use [GSP guicey module]((https://github.com/xvik/dropwizard-guicey/tree/master/guicey-server-pages)).

Sample application configures app:

```java
ServerPagesBundle.app("app", "/app/", "/")
            // rest path as index page
            .indexPage("person/")
            .build()
```       

(Application with name "app", with resources in classpath path "/app", served from root url)

Note that index page set to resource-powered template ` .indexPage("person/")`

Start application with arguments:

```
server config.yml
```

Static resource call:

```java
http://localhost:8080/style.css → src/main/resources/com/example/app/style.css
```

Direct template call:

```
http://localhost:8080/foo.ftl → src/main/resources/com/example/app/foo.ftl
```

Rest-driven template call:

```
http://localhost:8080/person/12 → /rest/com.example.app/person/12
```

Index page:

```
http://localhost:8080/ → /rest/com.example.app/person/
```