# Classpath scan

!!! summary 
    Use scan only for application package. When part of application extracted to its own library (usually already mature part) 
    create [guicey bundle](bundles.md) for it with explicit extensions definition. Use [manual bundles installation](configuration.md) 
    or [bundle lookup mechanism](bundles.md#bundle-lookup) to install custom bundles.

## Configuration

Classpath scanning is activated by specifying package to scan in bundle:
 
```java
GuiceBundle.builder()
    .enableAutoConfig("package.to.scan")
```

Or multiple packages:

```java
GuiceBundle.builder()
    .enableAutoConfig("com.mycompany.pkg1", "com.mycompany.pkg2")
```

If no packages specified, classpath scan would be activated for application package:

```java
GuiceBundle.builder()
    .enableAutoConfig()
```

(equivalent to `.enableAutoConfig(getClass().getPackage().getName())`

### Filter classes

By default, classpath scanner checks all available classes, and the only way to avoid extension 
recognition is `@InvisibleForScanner` annotation.

Now custom conditions could be specified:

```java
GuiceBundle.builder()
    .autoConfigFilter(ignoreAnnotated(Skip.class))
```

In this example, classes annotated with `@Skip` would not be recognized.

!!! note
    Assumed static import for `ClassFilters` utility, containing the most common cases.
    If required, raw predicate could be used:
    ```java
    .autoConfigFilter(cls -> !cls.isAnnotationPresent(Skip.class))
    ```

It is also possible now to implement spring-like approach when only annotated classes 
are recognized:

```java
GuiceBundle.builder()
    .autoConfigFilter(annotated(Component.class, Service.class))
```

Here only extensions annotated with `@Component` or `@Service` would be recognized.

!!! note
    This filter affects only extension search: installers and commands search does not use filters
    (because it would be confusing and error-prone).

!!! tip
    Multiple filters could be specified:
    ```java
    GuiceBundle.builder()
        .autoConfigFilter(annotated(Component.class, Service.class))
        .autoConfigFilter(ignoreAnnotated(Skip.class))
    ```

Auto config filter also affects extensions recognition from guice bindings and so
could be used for ignoring extensions from bindings.

It is also possible now to exclude some sub-packages from classpath scan:

```java
GuiceBundle.builder()
    .enableAutoConfig("com.company.app")
    .autoConfigFilter(ignorePackages("com.company.app.internal"))
```

### Private classes

By default, guicey does not search extensions in protected and package-private classes:

```java
public class Something {
    static class Ext1 implements Managed {}
    protected static class Ext2 implements Managed {}
}
```

But such extensions could be enabled with:

```java
GuiceBundle.builder()
    .option(GuiceyOptions.ScanProtectedClasses, true)
```

## How it works

When auto scan enabled:

* Extension installers are searched in classpath (classes implementing `FeatureInstaller`).
* Extensions are searched using registered installers (`FeatureInstaller#matches` method).
* If commands search is enabled (`.searchCommands()`), performs search for all classes extending `Command` and [install them into
bootstrap](commands.md#automatic-installation).

Classes are searched in specified packages and all their subpackages.
Abstract classes are ignored. 

Inner static classes are also resolved:

```java
public abstract class AbstractExceptionMapper<T extends Exception> implements ExceptionMapper<T> {

    @Provider
    public static class FooExceptionMapper extends AbstractExceptionMapper<IOException> { ... }

    @Provider
    public static class BarExceptionMapper extends AbstractExceptionMapper<ServletException> { ... }
}
```

`FooExceptionMapper` and `BarExceptionMapper` would be detected and installed.

## Hide class from scan

`@InvisibleForScanner` annotation hides class from scanner (for example, to install it manually or to avoid installation at all)

```java
@Provider
@InvisibleForScanner
public static class FooExceptionMapper extends AbstractExceptionMapper<IOException> { ... }
```

In this case `FooExceptionMapper` will be ignored by classpath scanner. But you still can install extension manually.

!!! tip
    If you can't use annotation on extension for some reason, you can simply [disable extension](disables.md#disable-extensions) 

## Motivation

Usually, dropwizard applications are not so big (middle to small) and all classes in application package are used (so you will load all of them in any case). 
 
Classpath scan looks for all classes in provided package(s) and loads all found classes. Usual solutions like [reflections](https://github.com/ronmamo/reflections), 
[fast scanner](https://github.com/lukehutch/fast-classpath-scanner) or even jersey's internal classpath scan parse class structure instead of loading classes. 
In general cases, it is better solution, but, as we use all application classes in any case, loading all of them a bit earlier is not a big deal. 
Moreover, operations with loaded classes are much simpler then working with class structure (and so installers matching logic becomes very simple).

Using classpath scan is very handy during development: you simply add features (resources, tasks, servlets etc) and they are automatically discovered and installer.
Actual application configuration could always be checked with [diagnostic output](diagnostic/configuration-report.md)),
so there should not be any problems for using classpath scan for production too.

!!! warning 
    It's a bad idea to use classpath scan for resolving extensions from 3rd party jars. Group extensions from external 
    jars into bundles. Usually, external libraries are well defined and all used extensions are already known and unlikely to change often, 
    so it's better to manually install them through custom guicey bundle: bundle "documents" extensions.
    If you want plug-n-play behaviour (bundle installed when jar appear in classpath) then use bundle lookup 
    (enabled by default) which could load bundles with [service loader definition](bundles.md#service-loader-lookup).
