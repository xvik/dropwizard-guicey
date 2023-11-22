# 5.10.0 Release Notes

* Update to dropwizard 2.1.10
* Add qualified configuration bindings
* Test improvements
    - DropwizardTestSupport and ClientSupport objects availability
    - Test client customization (ObjectSupport)
    - Improve generic testing
    - Support commands testing

## Qualified configuration bindings

1. Any configuration property could be bound in guice *just by annotating field or getter* with
   qualifier annotation (guice or jakarta).
2. Annotated fields with the same type and qualifier are bound aggregated with Set
3. Core dropwizard configuration objects could be bound with qualified overridden getter

```java
public class MyConfig extends Configuration {
    @Named("custom")
    private String prop1;
    @CustomQualifier
    private SubObj obj1 = new SubObj();

    public String getProp1() {
        return prop1;
    }
    
    public SubObj getSubObj() {
        return obj1;
    }

    @Named("metrics")  // dropwizard object bind
    @Override
    MetricsFactory getMetricsFactory() {
        return super.getMetricsFactory();
    }
}

public class SubObj {
    private String prop2;
    private String prop3;

    // aggregated binding (same type + qualifier)
    @Named("sub-prop")
    public String getProp2() {
        return prop2;
    }

    @Named("sub-prop")
    public String getProp3() {
        return prop3;
    }
}

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@BindingAnnotation
public @interface CustomQualifier {}
```

The following injections would be available:

```java
@Inject @Named("custom") String prop1;
@Inject @CustomQualifier SubObj obj1;
@Inject @Named("sub-prop") Set<String> prop23;
@Inject @Named("metrics") MetricsFactory metricsFactyry;
```

Configuration bindings report:

```java
GuiceBundle.builder()
        .printCustomConfigurationBindings()
```

Would show qualified bindings (with source property names in braces):

```
    Qualified bindings:
        @Named("metrics") MetricsFactory = MetricsFactory{frequency=1 minute, reporters=[], reportOnStop=false} (metrics)
        @CustomQualifier SubObj = ru.vyarus.dropwizard.guice.yaml.qualifier.QualifierSampleTest$SubObj@19e0dffe (obj1)
        @Named("sub-prop") Set<String> = (aggregated values)
            String = "2" (obj1.prop2)
            String = "3" (obj1.prop3)
        @Named("custom") String = "1" (prop1)
```

## Test improvements

### DropwizardTestSupport and ClientSupport objects availability

In junit 5 and spock 2 `DropwizardTestSupport` object could now be injected as test parameter
(or constructor, or lifecycle method parameter):

```java
@Test
public void testSomething(DropwizardTestSupport support) {
    
}
```

!!! note
    `GuiceyTestSupport extends DropwizardTestSupport` so this works for both web and core runs.

Also, context `DropwizardTestSupport` and `ClientSupport` objects now available statically (at the same thread):

```java
DropwizardTestSupport support = TestSupport.getContext();
ClientSupport client = TestSupport.getContextClient();
```

These static references would also work inside generic run callbacks, like:

```java
TestSupport.runCoreApp(App.class, injector -> {
        DropwizardTestSupport support = TestSupport.getContext();
});
```

(works for all `TestSupport.run*` methods)

New simplified (Void) runners now return DropwizardTestSupport, used for execution:

```java
DropwizardTestSupport support = TestSupport.runCoreApp(App.class);
```


### Test client customization (ObjectSupport)

First of all, the test client automatically configures now multipart feature if dropwizard-forms is available in classpath,

`JerseyClient` used in `ClientSupport` could be customized now using `TestClientFactory` implementation.

The default implementation looks like this:

```java
public class DefaultTestClientFactory implements TestClientFactory {

    @Override
    public JerseyClient create(final DropwizardTestSupport<?> support) {
        final JerseyClientBuilder builder = new JerseyClientBuilder()
                .register(new JacksonFeature(support.getEnvironment().getObjectMapper()))
                .property(ClientProperties.CONNECT_TIMEOUT, 1000)
                .property(ClientProperties.READ_TIMEOUT, 5000)
                .property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true);
        try {
            // when dropwizard-forms used automatically register multipart feature
            final Class<?> cls = Class.forName("org.glassfish.jersey.media.multipart.MultiPartFeature");
            builder.register(cls);
        } catch (Exception ignored) {
            // do nothing - no multipart feature available
        }
        return builder.build();
    }
}
```

Custom implementation could be specified directly in test annotation (junit 5, spock 2):

```java
@TestDropwizardApp(value = MyApp.class, clientFactory = CustomTestClientFactory.class)
```

All other builders also support client factory as an optional parameter.

### Improve generic testing

Generic builder was added to simplify application testing without test framework.
It supports almost everything as junit 5 extensions.

Example:

```java
DropwizardTestSupport support = TestSupport.build(App.class)
        .config("src/test/resources/path/to/test/config.yml")
        .configOverrides("foo: 2", "bar: 12")
        .hooks(new MyHook())
        .runCore()
```

or with action:

```java
Object serviceValue =  TestSupport.build(App.class)
        .config("src/test/resources/path/to/test/config.yml")
        .configOverrides("foo: 2", "bar: 12")
        .hooks(new MyHook())
        .runWeb(injector -> {
            return injector.getInstance(FooService.class).getSomething();
        })
```

Builder provide simple listener support to simplify setup and cleanup logic (without test framework):

```java
TestSupport.build(App.class)
        .listen(new TestSupportBuilder.TestListener<>() {
            public void setup(final DropwizardTestSupport<C> support) throws Exception {
                // do before test
            }
            ...
        })
        .runCore();
```

All listener methods are default so only required methods could be overridden.

`TestSupport` `run*` and "create" methods (`coreApp`, `webApp`) are powered now
with a new builder (and so many of them support string config overrides now as parameter).

### Support commands testing

An easy way for testing commands was added: `CommandTestSupport` object is
equivalent to `DropwizardTestSupport`, but for running commands.
It uses dropwizard `Cli` for arguments recognition and command selection.

The main difference with `DropwizardTestSupport` is that command execution is
a short-lived process and all assertions are possible only *after* the execution.
That's why command runner would include in the result all possible dropwizard objects,
created during execution (because it would be impossible to reference them after execution).

New builder (very similar to application execution builder, described above) was added to
simplify commands execution:

```java
CommandResult result = TestSupport.buildCommandRunner(App.class)
        .run("simple", "-u", "user")

Assertions.assertTrue(result.isSuccessful());
```

This runner could be used to run *any* command type (simple, configured, environment).
The type of command would define what objects would be present ofter the command execution
(for example, `Injector` would be available only for `EnvironmentCommand`).

!!! important
    Such run never fails with an exception: any appeared exception would be 
    stored inside the response:

    Assertions.assertFalse(result.isSuccessful());
    Assertions.assertEquals("Error message", result.getException().getMessage());

#### IO

Runner use System.in/err/out replacement. All output is intercepted and could be
asserted:

```java
Assertions.assertTrue(result.getOutput().contains("some text"))
```

`result.getOutput()` contains both `out` and `err` streams together
(the same way as user would see it in console). Error output is also available
separately with `result.getErrorOutput()`.

!!! note
    all output is always printed to console, so you could always see it after test execution
    (without additional actions)

User input commands are also mocked:

```java
CommandResult result = TestSupport.buildCommandRunner(App.class)
        .consoleInputs("1", "two", "something else")
        .run("quiz")
```

At least, the required number of answers must be provided (otherwise error would be thrown,
indicating not enough inputs)

!!! warning
    Due to IO overrides, command tests could not be run in parallel. 
    For junit 5, such tests could be annotated with `@Execution(SAME_THREAD)`
    (to prevent execution in parallel)

#### Configuration

Configuration could be applied the same way as in run builder: direct configuration instance,
file or (with) overrides:

```java
// override only
TestSupport.buildCommandRunner(App.class)
        .configOverride("foo: 12")
        .run("cfg");

// file with overrides
TestSupport.buildCommandRunner(App.class)
        .config("src/test/resources/path/to/config.yml")
        .configOverride("foo: 12")
        .run("cfg");

// direct config object
MyConfig config = new MyConfig();         
TestSupport.buildCommandRunner(App.class)
        .config(config)
        .run("cfg");
```

!!! note
    Config file should not be specified in command itself - build would add it, if required.
    Of course, it would not be a mistake to use config file directly in command:

    ```java
    TestSupport.buildCommandRunner(App.class)
        // note .config("...") was not used (otherwise two files would appear)!
        .run("cfg", "path/to/config.yml");
    ```

    But using builder seems like more clear option.

#### Listener

There is a simple listener support (like in application run builder) for setup-cleanup actions:

```java
TestSupport.buildCommandRunner(App.class)
        .listen(new CommandRunBuilder.CommandListener<>() {
            public void setup(String[] args) { ... }
            public void cleanup(CommandResult<TestConfiguration> result) { ... }
        })
        .run("cmd")
```

#### App startup fail

Command runner could also be used for application startup fail tests:

```java
CommandResult result = TestSupport.buildCommandRunner(App.class)
        .run("server")
```

or with shortcut:

```java
CommandResult result = TestSupport.buildCommandRunner(App.class)
        .runApp()
```

In case of application successful start, special check would immediately stop it
by throwing exception (result command would contain it), so such test would never freeze.

No additional mocks or rules required because running like this would not cause
`System.exist(1)` call, performed in `Application` class.