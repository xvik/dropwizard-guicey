# Lifecycle annotations

Allows using lifecycle annotations for initialization/destruction methods in Guice beans.
The main motivation is to replace `Managed` usage in places where it's simpler to just annotate a method rather than
register extension.

* `@PostConstruct` - same as `Managed.start()`
* `@PostStartup` - called after server startup (application completely started)
* `@PreDestroy` - same as `Managed.stop()`

## Setup

Maven:

```xml
<dependency>
  <groupId>ru.vyarus.guicey</groupId>
  <artifactId>guicey-lifecycle-annotations</artifactId>
  <version>{{ gradle.version }}</version>
</dependency>
```

Gradle:

```groovy
implementation 'ru.vyarus.guicey:lifecycle-annotations:{{ gradle.version }}'
```

Omit the version if the Guicey BOM is used.

## Usage

By default, no setup is required: the bundle will be loaded automatically with the bundle lookup mechanism (enabled by default).
So just add the jar to the classpath, and the annotations will work.

```java
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import ru.vyarus.guicey.annotations.lifecycle.PostStartup;

public class SampleBean {    

    @PostConstruct
    private void start() {
        // same time as Managed.start()
    }

    @PostStartup
    private void afterStartup() {
        // application completely started
    }

    @PreDestroy
    private void stop() {
        // same time as Managed.stop()
    }
}
```

* Annotated methods must not contain parameters. Methods can have any visibility.
* `@PostConstruct` or `@PostStartup` method failure fails the entire application startup (fail fast).
* `@PreDestroy` method failures are just logged to guarantee that all destroy methods will be processed.
* If both the current class and the superclass have annotated methods, both methods will be executed (the only obvious exception is overridden methods).

!!! important
    If bean is created on demand (lazy creation by guice JIT), annotated methods will still be called,
    even if actual lifecycle event was already passed. Warning log message will be printed to indicate this "not quite correct" execution,
    but you can be sure that your methods will always be processed.

### Reducing scope

Annotations are applied using guice [TypeListener api](http://google.github.io/guice/api-docs/latest/javadoc/index.html?com/google/inject/spi/TypeListener.html)
which means that all Guice beans are introspected for annotated methods.

If you want to limit the scope of processed beans, then register the bundle manually
(in this case lookup will be ignored):

```java
GuiceBundle.builder()
           .bundles(new LifecycleAnnotationsBundle("package.to.apply"))
           .build()
```

In this example, only beans in the specified package will be checked.

Also, direct `Matcher` implementation could be specified for more sophisticated cases.
For example, if I want to exclude only one class:

```java
new LifecycleAnnotationsBundle(new AbstractMatcher<TypeLiteral<?>>() {                               
           @Override
           public boolean matches(TypeLiteral<?> o) {
               return o.getRawType() != SomeExcludedBean.class;
           }
       })
```
