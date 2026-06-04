# 3rd party extensions integration

It is extremely simple in JUnit 5 to [write extensions](https://junit.org/junit5/docs/current/user-guide/#extensions).
If you write your own extension, you can easily integrate with Guicey or Dropwizard extensions.

!!! tip
    In many cases, it would be easier to write a custom Guicey [setup object](setup-object.md)
    which provides almost the same abilities as JUnit extensions plus Guicey awareness.
    All field-based extensions in Guicey are implemented with setup objects.

## Guicey side

If you already have a JUnit extension that stores something in `ExtensionContext`, then you can:

1. Bind value into [configuration directly](config.md#configuration-from-3rd-party-extensions):
    `.configOverrideByExtension(ExtensionContext.Namespace.GLOBAL, "ext-key", "config.key")`
2. Bind JUnit `ExtensionContext` as a test method parameter (and access storage manually):
```java
@BeforeAll
public static void beforeAll(ExtensionContext junitContext) {
    ...
}
```
3. Inside a [setup object](setup-object.md), access the JUnit context:
```java
public class MyExt implements GuiceyEnvironmentSetup {
    @Override
    public Object setup(TestExtension extension) throws Exception {
        ExtensionContext context = extension.getJunitContext();
    }
}
```

## Extension side

There are special static methods allowing you to obtain main test objects:

* `GuiceyExtensionsSupport.lookupSupport(extensionContext)` -> `Optional<DropwizardTestSupport>`
* `GuiceyExtensionsSupport.lookupInjector(extensionContext)` -> `Optional<Injector>`
* `GuiceyExtensionsSupport.lookupClient(extensionContext)` -> `Optional<ClientSupport>`

For example:

```java
public class MyExtension implements BeforeEachCallback {
    
    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        Injector injector = GuiceyExtensionsSupport.lookupInjector(context).get();
        ...
    }
}
```

(Guicey holds test state in JUnit test-specific storages, and that's why a test context is required)

!!! warning
    There is no way in JUnit to order extensions, so you will have to make sure that your extension
    will be declared after the Guicey extension (`@TestGuiceyApp` or `@TestDropwizardApp`).

There is intentionally no direct API for applying configuration overrides from
3rd party extensions because it would not be obvious. Instead, you should always
declare overridden value in extension declaration. Either use instance getter:

```java
@RegisterExtension
static MyExtension ext = new MyExtension()

@RegisterExtension
static TestGuiceyAppExtension dw = TestGuiceyAppExtension.forApp(App.class)
        .configOverride("some.key", ()-> ext.getValue())
        .create()
```

Or store the value [inside the JUnit store](config.md#configuration-from-3rd-party-extensions) and then reference it:

```java
@RegisterExtension
static TestGuiceyAppExtension app = TestGuiceyAppExtension.forApp(App.class)
        .configOverrideByExtension(ExtensionContext.Namespace.GLOBAL, "ext1")
        .create();
```
