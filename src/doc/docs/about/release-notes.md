# 5.3.0 Release Notes

!!! summary ""
    [5.0.0 release notes](http://xvik.github.io/dropwizard-guicey/5.0.0/about/release-notes/)
    [5.2.0 release notes](http://xvik.github.io/dropwizard-guicey/5.1.0/about/release-notes/)

Release targets gucie 5 compatibility. There are no changes in guicey itself (to simplify migration). 

* [General](#general)
* [JUnit 5](#junit-5) 
* [Bugfixes](#bugfixes)

## General

Guice updated to [5.0.1](https://github.com/google/guice/wiki/Guice501)

Dropwizard updated to 2.0.20.

## JUnit 5

For junit 5 extensions it is now possible to use deferred values resolution for config overrides. 
This is useful when overriding value must be provided by other extension.

For example:

```java
@RegisterExtension
@Order(1)
static FooExtension ext = new FooExtension();

@RegisterExtension
@Order(2)
static TestGuiceyAppExtension app = TestGuiceyAppExtension.forApp(AutoScanApplication.class)
        .config("src/test/resources/ru/vyarus/dropwizard/guice/config.yml")
        .configOverrides("foo: 1")
        .configOverride("bar", () -> ext.getValue())
        .configOverrides(new ConfigOverrideValue("baa", () -> "44"))
        .create();
```

New `configOverride("bar", () -> ext.getValue())` method accepts `Supplier<String>`.

And more general `.configOverrides(new ConfigOverrideValue("baa", () -> "44"))` directly accepts 
`ConfigOverride` object. 

BUT Supplied `ConfigOverride` object implementations must implement `ru.vyarus.dropwizard.guice.test.util.ConfigurablePrefix`.
This restriction required for proper parallel tests support: extensions generate unique prefix for
each test because eventually all config overrides are stored to system properties.

## Bugfixes

* [151](https://github.com/xvik/dropwizard-guicey/issues/151) - move config overrides initialization from constructor in GuiceyAppRule (junit4)