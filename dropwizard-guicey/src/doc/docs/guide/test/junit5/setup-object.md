# Test environment setup

It is often required to prepare test environment before starting dropwizard application.
Normally, such cases require writing custom junit extensions. In order to simplify
environment setup, guicey provides `TestEnviromentSetup` interface.

Setup objects are called before application startup and could directly apply (through parameter)
configuration overrides and hooks.

!!! info
    As [hooks](hooks.md) could modify application configuration, setup object modifies
    test extension configuration (hook - extra application functionality, setup object - extra test functionality).

For example, suppose you need to set up a database before test:

```java
public class TestDbSetup implements TestEnvironmentSetup {

    @Override
    public Object setup(TestExtension extension) throws Exception {
        // pseudo code
        Db db = DbFactory.startTestDb();
        // register required configuration
        extension
                .configOverride("database.url", ()-> db.getUrl())
                .configOverride("database.user", ()-> db.getUser())
                .configOverride("database.password", ()-> db.getPassword);
        // assuming object implements Closable
        return db;
    }
}
```

It is not required to return anything, only if something needs to be closed after application shutdown:
objects other than `Closable` (`AutoClosable`) or `org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource`
simply ignored.
This approach (only one method) simplifies interface usage with lambdas.

Setup object might be declared in extension annotation: 

```java
@TestGuiceyApp(value=App.class, setup=TestDbSetup.class)
```

Or in manual registration:

```java
@RegisterExtension
TestGuiceyAppExtension ext = TestGuiceyAppExtension.forApp(App.class)
        // as class
        .setup(TestDbSetup.class)
        // or as instance
        .setup(new TestDbSetup())
```

Or with lambda:

```java
.setup(ext -> {
        Db db = new Db();
        ext.configOverride("db.url", ()->db.getUrl())
        return db;
})
```

## Setup fields

Alternatively, setup objects might be declared simply in test fields:

```java
@EnableSetup
static TestEnvironmentSetup db = ext -> {
            Db db = new Db();
            ext.configOverride("db.url", ()->db.getUrl())
            return db;
        };
```

or

```java
@EnableSetup
static TestDbSetup db = new TestDbSetup()
```

This could be extremely useful if you need to unify setup logic for multiple tests,
but use different extension declarations in test. In this case simply move field
declaration into base test class:

```java
public abstract class BaseTest {
    
    @EnableSetup
    static TestDbSetup db = new TestDbSetup();
}
```

!!! note
    To avoid confusion with guicey hooks: setup object required to prepare test environment before test (and apply
    required configurations) whereas hooks is a general mechanism for application customization (not only in tests).
    Setup objects are executed before application startup (before `DropwizardTestSupport` object creation) and hooks
    are executed by started application.

### Custom configuration block

To simplify field-based declarations, custom (free) block added (`.with()`):

```java
@RegisterExtension
static TestGuiceyAppExtension ext = TestGuiceyAppExtension.forApp(..)
        ...
        .with(builder -> {
            if (...) {
               builder.configOverrides("foo.bar", 12); 
            }
        }) 
```

And the same for setup objects:

```java
@EnableSetup
static TestEnvironmentSetup setup = ext ->
        ...
        .with(builder -> {
            ...
        }) 
```

## Builder configuration

`TestExtension` builder provides almost the same options as the main guice extension builder (when declared in field) 

| Method                                                                   | Description                                                                                            | Example                                                                                     |
|--------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------|
| `.config(ThrowingSupplier)`                                              | Manual configuration object creation (config overrides will not work)                                  | `.config(()-> new MyConfig())`                                                              |
| `.configOverrides(String...)`                                            | Multiple configuration override values in "key: value" form.                                           | `.configOverrides("foo: 10", "bar: 12")`                                                    |
| `.configOverrides(ConfigOverride & ConfigurablePrefix)`                  | Config override object (used for deferred values)                                                      | `.configOverrides(new ConfigOverrideValue("baa", () -> "44"))`                              |
| `.configOverride(String, String)`                                        | Single config path override                                                                            | `.configOverride("some.foo", "12")`                                                         |
| `.configOverride(String, Supplier<String>)`                              | Deferred config override value                                                                         | `.configOverride("foo", () -> "1")`                                                         |
| `.configOverrideByExtension(ExtensionContext.Namespace, String)`         | 3rd party junit extension integration                                                                  | `.configOverrideByExtension(ExtensionContext.Namespace.GLOBAL, "foo")`                      |
| `.configOverrideByExtension(ExtensionContext.Namespace, String, String)` | 3rd party junit extension integration                                                                  | `.configOverrideByExtension(ExtensionContext.Namespace.create("sample"), "storKey", "foo")` |
| `.hooks(GuiceyConfigurationHook)`                                        | Hooks registration                                                                                     | `.hooks(builder -> builder.disableExtensions(Something.class))`                             |
| `.configModifiers(ConfigModifier...)`                                    | Config modifier registration                                                                           | `.<MyConfig>configModifiers(config -> config.bar = 11)`                                     |
| `.injectOnce()`                                                          | Process test fields injection only once (for same test instance)                                       |                                                                                             |
| `.debug()`                                                               | Enable [debug](debug.md) output                                                                        |                                                                                             |
| `.reuseApplication()`                                                    | Use [the same application](unification.md#reuse-application-between-tests) instance for multiple tests |                                                                                             |
| `.disableDefaultExtensions()`                                            | Disable setup objects loading with service lookup (and so default extensions)                          |                                                                                             |
| `.clientFactory(TestClientFactory)`                                      | Custom [web client](client.md) client factory (used in `ClientSupport`)                                |                                                                                             |

Specific options:

| Method                            | Description                                                                  |
|-----------------------------------|------------------------------------------------------------------------------|
| `.isDebug()`                      | Identifies activated debug mode                                              |
| `.isApplicationStartedForClass()` | Shortcut to differentiate application started for test calss or every method |
| `.getJunitContext()`              | Access junit `ExtensionContext`                                              |

### Lifecycle

Setup object could react on test lifecycle events: `.listen(TestExecutionListener)`:

```java
public interface TestExecutionListener {
    default void starting(final EventContext context) throws Exception {}
    default void started(final EventContext context) throws Exception {}
    default void beforeAll(final EventContext context) throws Exception {}
    default void beforeEach(final EventContext context) throws Exception {}
    default void afterEach(final EventContext context) throws Exception {}
    default void afterAll(final EventContext context) throws Exception {}
    default void stopping(final EventContext context) throws Exception {}
    default void stopped(final EventContext context) throws Exception {}
}
```


Complex setup objects might simply `implement TestExecutionListener` and register self: 


```java
public class MySetup implements TestEnvironmentSetup, TestExecutionListener {
    @Override
    public Object setup(TestExtension extension) throws Exception {
        extension.listen(this);
    }

    @Override
    public void started(final EventContext context) throws Exception {
        // something
    }
}
```

To simplify usage with setup fields, separate listener methods available to use with lambdas:

```java
public class Test {

    @EnableSetup
    static TestDbSetup db = ext - > ext
            .onApplicationStarting(event -> ...)
            .onApplicationStart(event -> ...)
            .onBeforeAll(event -> ...)
            .onBeforeEach(event -> ...)
            .onAfterEach(event -> ...)
            .onAfterAll(event -> ...)
            .onApplicationStopping(event -> ...)
            .onApplicationStop(event -> ...)
}
```

Events:

| Listener     | Shortcut method         | Description                                                                          | Junit phase             |
|--------------|-------------------------|--------------------------------------------------------------------------------------|-------------------------|
| `starting`   | `onApplicationStarting` | Just before application starting                                                     | BeforeAll or BeforeEach |
| `started`    | `onApplicationStart`    | Application started                                                                  | BeforeAll or BeforeEach |
| `beforeAll`  | `onBeforeAll`           | Before all test methods (**might not be called** if extension registered per method) | BeforeAll or not called |
| `beforeEach` | `onBeforeEach`          | Before each test method                                                              | BeforeEach              |
| `afterEach`  | `onAfterEach`           | After each test method                                                               | AfterEach               |
| `afterAll`   | `onAfterAll`            | After all test methods (**might not be called** if extension registered per method)  | AfterAll or not called  |
| `stopping`   | `onApplicationStopping` | Just before application stopping                                                     | AfterAll or AfterEach   |
| `stopped`    | `onApplicationStop`     | Application stopped                                                                  | AfterAll or AfterEach   |

`EventContext` parameter provides access for guice injector, DropwizardTestSupport object and junit 5 context.

As you can see, events cover all junit lifecycle events together with application specific
events. Which makes setup objects a complete alternative to pure junit extensions.

## Auto lookup

Custom `TestEnvironmentSetup` objects could be loaded automatically
with service loader. New default extensions already use service loader.

To enable automatic loading of custom extension add:
`META-INF/services/ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup`

And put there required setup object classes (one per line), like this:

```
ru.vyarus.dropwizard.guice.test.jupiter.ext.log.RecordedLogsSupport
ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.RestStubSupport
ru.vyarus.dropwizard.guice.test.jupiter.ext.stub.StubsSupport
ru.vyarus.dropwizard.guice.test.jupiter.ext.mock.MocksSupport
```

Now, when setup objects have more abilities, more custom test extensions could be implemented
(see new filed-based extensions below). Automatic installation for such 3rd party
extensions (using service loader) should simplify overall usage.

!!! note
    Service loading for extensions could be disabled (together with new default extensions):
    ```java
    @TestGuiceyApp(.., useDefaultExtensions = false)
    ```

## Annotated fields support

`TestExtension` builder provides a special method to search annotated test fields: `.findAnnotatedFields()`.

```java
public class Test {
     @MyAnn
     Base field;
}
```

```java
public class CustomFieldsSupport implements TestEnvironmentSetup {
    @Override
    public Object setup(TestExtension extension) throws Exception {

        List<AnnotatedField<MyAnn, Base>> fields = extension
                .findAnnotatedFields(MyAnn.class, Base.class);
    }
```

Out of the box, API provides many checks, like required base class (it could be Object to avoid check):
if annotated field type is different - error would be thrown.

Returned object is also an abstraction: `AnnotatedField` - it simplifies working with filed value,
plus contains additional checks.

The main idea is keeping annotation, filed and actual value (that must be injected into test field)
in one object (for simplicity - no need to maintain external state).

### Writing annotated field support

There is a special **base class** `AnnotatedTestFieldSetup` which implements base fields workflow
(including proper nested tests support).

Use this class if you want to implement new field annotation (`@MyAnnotation`) support:

```java
public class MyFieldsSupport extends AnnotatedTestFieldSetup<MyAnnotation, Object>  
```

If your field value would always base on some class then specify it to automatically
apply related field validations: `AnnotatedTestFieldSetup<MyAnnotation, MyBaseClass>`

All current field extensions are using this base class, so you can see usage examples in:

* `StubFieldsSupport` - [@StubBean](stubs.md)
* `MockFieldsSupport` - [@MockBean](mocks.md)
* `SpyFieldsSupport` - [@SpyBean](spies.md)
* `LogFieldsSupport` - [@RecordLogs](logs.md)
* `TrackerFieldsSupport` - [@TrackBean](tracks.md)
* `RestStubFieldsSupport` - [@StubRest](rest.md)

Base class would search for all annotated fields and call other methods only if 
anything was found.

!!! important
    It is recommended to implement core extension logic inside the hook and use
    setup object obly to configure that hook. This way setup object would be simpler.
    (all extensions above use separate hooks).

The following methods should be implemented:

| Method               | Description                                                                                                                      | Stage                                                        |
|----------------------|----------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------|
| fieldDetected        | Validate resolved field, if required. Anything that could not be checked automatically                                           | beforeAll or beforeEach, app not started                     |
| registerHooks        | Register hook instance (hook used to apply extensions, override guice bindings etc.).                                            | beforeAll or beforeEach, app not started                     |
| initializeField      | Here value must be prepared to inject into annotated field. Or user-provided value must be validated                             | beforeAll or beforeEach, app not started                     |
| beforeValueInjection | Called just before injecting value into test field. Good point to apply remaining validations (e.g. requireing started injector) | beforeAll and beforeEach (called up to 2 times), app started |
| injectFieldValue     | Called to provide field value for injection (if pre-initializerd by user - method not called)                                    | beforeAll and beforeEach (called up to 2 times), app started |
| report               | Debug report (list detected fields). Report is called when root extension debug is enabled                                       | beforeAll or beforeEach, app started                         |
| beforeTest           | Called to call lifecycle method before test (like state clearing)                                                                | beforeEach, app started                         |
| afterTest            | Called to call lifecycle method after test (like state clearing)                                                                 | beforeEach, app started                         |

Take a look at `MockFieldsSupport` - it is a simple and easy to understand implementation.

#### fieldDetected

Method called as soon as field is detected:

* Ideal place for an additional validations (`TrackerFieldsSupport` validates field type there) 
* This is the earliest point: `LogFieldsSupport` use it to activate logger immediately

#### registerHooks

Usually simple hook registration. Only `RestStubFieldsSupport` use it to register 2 hooks
(second hook validates application scope: in theory could be implemented in one hook but guicey implements generic 
hooks which could be used without junit).

#### initializeField

Here we validate user-provided value or create new value.

For example, mocks hook (`MockFieldsSupport`):

```java
@Override
@SuppressWarnings("unchecked")
protected <K> void initializeField(final AnnotatedField<MockBean, Object> field, final Object userValue) {
    final Class<? super K> type = field.getType();
    if (userValue != null) {
        Preconditions.checkState(MockUtil.isMock(userValue), getDeclarationErrorPrefix(field)
                + "initialized instance is not a mockito mock object. Either provide correct mock or remove value "
                + "and let extension create mock automatically.");
        hook.mock(type, (K) userValue);
    } else {
        // no need to store custom data for manual value - injectFieldValue not called for manual values 
        field.setCustomData(FIELD_MOCK, hook.mock(type));
    }
}
```

Note that value is stored inside an `AnnotatedField` object: `field.setCustomData(FIELD_MOCK, hook.mock(type));`
(for user-provided value, it is stored automatically).

This is a not required step: for example, `LogFieldsSupport` create value object just after field detection
(because logger must be appended as soon as possible), and so ignored `initializeField` method.

Another example is `StubFieldsSupport` - where `initializeField` method used just for 
stub registration in hook. Value for injection into test field is obtained later directly
from guice injector (stub could be declared by class - instance is guice managed).

#### beforeValueInjection

For remaining validation (when injector is required). For example, `SpyFieldsSupport`
use it to validatate if target bean is managed by guice (spy use AOP and can't work with bean bound by instance)
Same story for `TrackerFieldsSupport`.

There is even a helper method to validate non-instance bindings: `isInstanceBinding(binding)`

#### injectFieldValue

Method called **only for not pre-initialized** fields (no user value). 

In most cases, it just provides a value, created in `initializeField`:

```java
@Override
protected Object injectFieldValue(final EventContext context, final AnnotatedField<MockBean, Object> field) {
    return Preconditions.checkNotNull(field.getCustomData(FIELD_MOCK), "Mock not created");
}
```

Stubs extension rely on guice context (because stub could be guice-meneged):

```java
@Override
protected Object injectFieldValue(final EventContext context, final AnnotatedField<StubBean, Object> field) {
    // if not declared, stub value created by guice
    return context.getBean(field.getAnnotation().value());
}
```

#### report

Report assumed to show detected fields when root extension debug is enabled. See example report in any extension.

#### beforeTest and afterTest

Special methods for implementing field value lifecycle. 
Almost all values have to be reset after each test method (mocks, spies, stubs etc.). 

Example from logs extension:

```java
 @Override
protected void afterTest(final EventContext context,
                         final AnnotatedField<RecordLogs, RecordedLogs> field, final RecordedLogs value) {
    if (field.getAnnotation().autoReset()) {
        value.clear();
    }
}
```

Mocks and speies use this method also to print summary report (if requested in annoation):

```java
@Override
@SuppressWarnings("PMD.SystemPrintln")
protected void afterTest(final EventContext context,
                         final AnnotatedField<MockBean, Object> field, final Object value) {
    if (field.getAnnotation().printSummary()) {
        final String res = Mockito.mockingDetails(value).printInvocations();
        System.out.println(PrintUtils.getPerformanceReportSeparator(context.getJunitContext())
                + "@" + MockBean.class.getSimpleName() + " stats on [After each] for "
                + TestSetupUtils.getContextTestName(context.getJunitContext()) + ":\n\n"
                + Arrays.stream(res.split("\n")).map(s -> "\t" + s).collect(Collectors.joining("\n")));
    }
    if (field.getAnnotation().autoReset()) {
        Mockito.reset(value);
    }
}
```
