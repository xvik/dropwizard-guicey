# 3rd party extensions integration

It is extremely simple in JUnit 5 to [write extensions](https://junit.org/junit5/docs/current/user-guide/#extensions).
If you do your own extension, you can easily integrate with guicey or dropwizard extensions.

!!! tip
    In many cases, it would be easier to write a custom guicey [setup object](setup-object.md) 
    which provides almost the same abilities as junit extensions plus guicey awareness.
    All field-based extensions in guicey are implemented with setup objects.

## Guicey side

If you already have a junit extension that stores something in `ExtensionContext` then you can:

1. Bind value into [configuration directly](config.md#configuration-from-3rd-party-extensions): 
    `.configOverrideByExtension(ExtensionContext.Namespace.GLOBAL, "ext-key", "config.key")` 
2. Bind junit `ExtensionContext` as test method parameter (and access storage manually):
```java
@BeforeAll
public static void beforeAll(ExtensionContext junitContext) {
    ...
}
```
3. Inside [setup object](setup-object.md) access junit context: 
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

(guicey holds test state in junit test-specific storages and that's why test context is required)

!!! warning
    There is no way in junit to order extensions, so you will have to make sure that your extension
    will be declared after guicey extension (`@TestGuiceyApp` or `@TestDropwizardApp`).

There is intentionally no direct api for applying configuration overrides from
3rd party extensions because it would be not obvious. Instead, you should always
declare overridden value in extension declaration. Either use instance getter:

```java
@RegisterExtension
static MyExtension ext = new MyExtension()

@RegisterExtension
static TestGuiceyAppExtension dw = TestGuiceyAppExtension.forApp(App.class)
        .configOverride("some.key", ()-> ext.getValue())
        .create()
```

Or store value [inside junit store](#configuration-from-3rd-party-extensions) and then reference it:

```java
@RegisterExtension
static TestGuiceyAppExtension app = TestGuiceyAppExtension.forApp(App.class)
        .configOverrideByExtension(ExtensionContext.Namespace.GLOBAL, "ext1")
        .create();
```
