# Lifecycle annotations

!!! summary ""
    [Extensions project](https://github.com/xvik/dropwizard-guicey-ext/tree/master/guicey-lifecycle-annotations) module

Allows using lifecycle annotations for initialization/destruction methods in guice beans.
Main motivation is to replace `Managed` usage in places where it's simpler to just annotate method, rather than
register extension.

* `@PostCostruct` - same as `Managed.start()`
* `@PostStartup` - called after server startup (application completely started)
* `@PreDestroy` - same as `Managed.stop()`

## Setup

[![JCenter](https://img.shields.io/bintray/v/vyarus/xvik/dropwizard-guicey-ext.svg?label=jcenter)](https://bintray.com/vyarus/xvik/dropwizard-guicey-ext/_latestVersion)
[![Maven Central](https://img.shields.io/maven-central/v/ru.vyarus.guicey/guicey-lifecycle-annotations.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/ru.vyarus.guicey/guicey-lifecycle-annotations)

Avoid version in dependency declaration below if you use [extensions BOM](../guicey-bom). 

Maven:

```xml
<dependency>
  <groupId>ru.vyarus.guicey</groupId>
  <artifactId>guicey-lifecycle-annotations</artifactId>
  <version>0.6.0</version>
</dependency>
```

Gradle:

```groovy
compile 'ru.vyarus.guicey:lifecycle-annotations:0.6.0'
```

See the most recent version in the badge above.


## Usage

By default no setup required: bundle will be loaded automatically with the bundles lookup mechanism (enabled by default).
So just add jar into classpath and annotations will work.

```java
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
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

* Annotated methods must not contain parameters. Method could have any visibility.
* `@PostConstruct` or `@PostStartup` methods fail fails entire application startup (fail fast)
* `@PreDestroy` method fails are just logged to guarantee that all destroy methods will be procesed
* If both current class and super class have annotated methods - both methods will be executed (the only obvious exception is overridden methods)

!!! important   
    If bean is created on demand (lazy creation by guice JIT), annotated methods will still be called,
    even if actual lifecycle event was already passed. Warning log message will be printed to indicate this "not quite correct" execution,
    but you can be sure that your methods will always be processed.

### Reducing scope

Annotations are applied using guice [TypeListener api](http://google.github.io/guice/api-docs/latest/javadoc/index.html?com/google/inject/spi/TypeListener.html)
which means that all guice beans are introspected for annotated methods.

If you want to limit the scope of processed beans then register bundle manually 
(in this case lookup will be ignored):

```java
GuiceBundle.builder()
           .bundles(new LifecycleAnnotationsBundle("package.to.apply"))
           .build()
```

In this example only beans lying in specified package will be checked. 

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