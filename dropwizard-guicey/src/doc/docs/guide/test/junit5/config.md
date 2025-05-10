# Application configuration

!!! note
    In terms of configuration, both extensions (`@TestGuiceyApp` and `@TestDropwizardApp`) 
    are equal, so all examples would show just one of them.

    Also, annotation provides the same options as field-based extension declaration,
    so if something is shown for annotation - the same could be done with builder.

Application could be started with an external configuration file:

```java
@TestGuiceyApp(value = MyApplication.class,
    config = "path/to/my/test-config.yml"
public class MyTest {
```

Or just declare required values:

```java
@TestGuiceyApp(value = MyApplication.class,
    configOverride = {
            "foo: 2",
            "bar: 12"
    })
public class ConfigOverrideTest {
```

(note that overriding declaration follows yaml format "key: value")

Or use both at once (here overrides will override file values):

```java
@TestGuiceyApp(value = MyApplication.class,
    config = 'path/to/my/config.yml',
    configOverride = {
            "foo: 2",
            "bar: 12"
    })
class ConfigOverrideTest {
```

## Manual configuration object

Normally, either empty configuration object created (if a configuration file not provided) 
or it created from a specified file.

It is also possible to manually construct configuration object instance in
junit5 extension (for both lightweight and full app tests):

```java
@RegisterExtension
static TestGuiceyAppExtension ext = TestGuiceyAppExtension.forApp(..)
        .config(() -> new MyConfig())
        ...
```

Or in setup object:

```java
@EnableSetup
static TestEnvironmentSetup setup = ext -> ext.config(() -> new MyConfig())
```

!!! tip
    Pay attention to how setup objects could be used for configuration modification:
    it is often easier to declare test extension in base class and use setup objects
    for test-specific modifications.

!!! important
    Configuration overrides **would not work** with manually created configuration objects.
    Use configuration modifiers with manual configs.


## Configuration modifiers

Dropwizard configuration overrides mechanism is limited (for example, it would not work for a collection property).

Configuration modifier is an alternative mechanism when all changes are performed on
configuration instance.

Modifier could be used as lambda:

```java
@RegisterExtension
static TestGuiceyAppExtension ext = TestGuiceyAppExtension.forApp(..)
        .configModifiers(config -> config.getSomething().setFoo(12))
        ...
```

Or in setup object:

```java
@EnableSetup
static TestEnvironmentSetup setup = ext -> 
        ext.configModifiers(config -> config.getSomething().setFoo(12))
```

Modifier could be declared in class:

```java
public class MyModifier implements ConfigModifier<MyConfig> {
    @Override
    public void modify(MyConfig config) throws Exception {
        config.getSomething().setFoo(12);
    }
}

@TestGuiceyApp(.., configModifiers = MyModifier.class)
```

!!! tip
    Modifier could be used with both manual configuration or usual (yaml) configuration.
    Configuration modifiers also could be used together with configuration overrides.

!!! warning "Limitation"
    Configuration modifiers are called after dropwizard logging configuration,
    so logging is the only thing that can't be configured (use configuration overrides for logging)

## Deferred configuration

If you need to configure value, supplied by some other extension, or value may be resolved only
after test start, then static overrides declaration is not an option. In this case use
[alternative extensions declaration](run.md#alternative-declaration) which provides additional 
config override methods:

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

In most cases `configOverride("bar", () -> ext.getValue())` would be enough to configure a supplier instead
of static value.

In more complex cases, you can use custom implementations of `ConfigOverride`. 

!!! warning ""
    Guicey have to accept only `ConfigOverride` objects implementing custom 
    `ru.vyarus.dropwizard.guice.test.util.ConfigurablePrefix` interface. 
    In order to support parallel tests guicey generates unique config prefix for each test
    (because all overrides eventually stored to system properties) and so it needs a way
    to set this prefix into custom `ConfigOverride` objects.

## Configuration from 3rd party extensions

If you have junit extension (e.g. which starts db for test) and you need 
to apply configuration overrides from that extension, then you should simply
store required values inside junit storage:

```java
public class ConfigExtension implements BeforeAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        // do something and then store value
        context.getStore(ExtensionContext.Namespace.GLOBAL).put("ext1", 10);
    }
}
```

And map overrides directly from store using `configOverrideByExtension` method:

```java
@ExtendWith(ConfigExtension.class)
public class SampleTest {
    
    @RegisterExtension
    static TestGuiceyAppExtension app = TestGuiceyAppExtension.forApp(App.class)
            .configOverrideByExtension(ExtensionContext.Namespace.GLOBAL, "ext1")
            .create();
}
```

Here, value applied by extension under key `ext1` would be applied to configuration `ext1` path.
If you need to use different configuration key:

```java
.configOverrideByExtension(ExtensionContext.Namespace.GLOBAL, "ext1", "key")
```

!!! tip
    You can use [setup objects](setup-object.md) instead of custom junit extensions for test environment setup


