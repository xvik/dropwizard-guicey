# 7.1.0 Release Notes

* Update to dropwizard 4.0.4
* Add qualified configuration bindings
* Test improvements:
    - DropwizardTestSupport and ClientSupport objects availability
    - Web client improvements (ClientSupport)
    - Improve generic testing
    - Support commands testing

## Qualified configuration bindings

1. Any configuration property could be bound to guice *just by annotating field or getter* with 
qualifier annotation (guice or jakarta).
2. Annotated fields with the same type and qualifier are bound aggregated with `Set`
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

!!! note
    Properties are grouped by exact type and annotation (exactly the same binding keys), 
    so don't expect more complex grouping (for example, by some base class). 

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

Guice modules and guicey bundles could also access annotated values 
(through `DropwizardAwareModule` and `GuiceyEnvironment` in `GuiceyBundle#run`):

* `.annotatedValue(Names.named("custom"))` - access by (equal) annotation instance (for annotations with state)
* `.annotatedValue(CustomQualifier.class)` - access by annotation type

More related methods added for `ConfigurationTree` object: 

* `findAllByAnnotation` - find all annotated paths
* `findByAnnotation` - find exactly one annotated path (fail if more found) 
* `annotatatedValues` - all non-null values from annotated paths

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

### Web client improvements (ClientSupport)

#### Simple methods

The client now contains simple GET/POST/PUT/DELETE methods for simple cases:

```java
@Test
public void testWeb(ClientSupport client) {
    // get with result
    Result res = client.get("rest/sample", Result.class);
    
    // post without result (void)
    client.post("rest/action", new PostObject(), null);
}
```

All methods:

1. Methods accept paths relative to server root. In the example above: "http://localhost:8080/rest/sample"
2. Could return mapped response. 
3. For void calls, use null instead of the result type. In this case, only 200 and 204 (no content) responses 
    would be considered successful
   
POST and PUT also accept (body) object to send. 
But methods does not allow multipart execution.

These methods could be used as examples for jersey client usage.

There is also a new helper method: `client.basePathRoot()` returning the base server path (localhost + port); 

#### Default client

`JerseyClient` used inside `ClientSupport` now automatically configures 
multipart feature if `dropwizard-forms` is in classpath (so the client could be used
for sending multipart data).

Request and response logging is enabled by default now to simplify writing (and debugging) tests. 
By default, all messages are written directly into console to guarantee client
actions visibility (logging might not be configured in tests).

Example output:

```

[Client action]---------------------------------------------{
1 * Sending client request on thread main
1 > GET http://localhost:8080/sample/get

}----------------------------------------------------------


[Client action]---------------------------------------------{
1 * Client response received on thread main
1 < 200
1 < Content-Length: 13
1 < Content-Type: application/json
1 < Date: Mon, 27 Nov 2023 10:00:40 GMT
1 < Vary: Accept-Encoding
{"foo":"get"}

}----------------------------------------------------------
```

Console output might be disabled with a system proprty:

```java
// shortcut sets DefaultTestClientFactory.USE_LOGGER property
DefaultTestClientFactory.disableConsoleLog()
```

With it, everything would be logged into `ClientSupport` logger under INFO
(most likely, would be invisible in the most logger configurations, but could be enabled).


To reset property (and get logs back into console) use:

```java
DefaultTestClientFactory.enableConsoleLog()
```

!!! note
    Static methods added not directly into `ClientSupport` because this is 
    the default client factory feature. You might use a completely different factory.

#### Custom client factory

`JerseyClient` used in `ClientSupport` could be customized now using `TestClientFactory` implementation.

Simple factory example:

```java
public class SimpleTestClientFactory implements TestClientFactory {

    @Override
    public JerseyClient create(final DropwizardTestSupport<?> support) {
        return new JerseyClientBuilder()
                .register(new JacksonFeature(support.getEnvironment().getObjectMapper()))
                .property(ClientProperties.CONNECT_TIMEOUT, 1000)
                .property(ClientProperties.READ_TIMEOUT, 5000)
                .property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
                .build();
    }
}
```

!!! tip
    See `DefaultTestClientFactory` implementation (it's a bit more complex)

Custom factory could be specified directly in test annotation (junit 5, spock 2):

```java
@TestDropwizardApp(value = MyApp.class, clientFactory = CustomTestClientFactory.class)
```

All other builders also support client factory as an optional parameter.

### Improve generic testing

#### Generic run builder

Generic builder was added to simplify application testing without test framework.
It supports almost everything as junit 5 extensions.

Example:

```java
RunResult result = TestSupport.build(App.class)
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
with a new builder (and so many of them support string config overrides as parameter).

#### Run methods improvements

Extra `run*` methods added for simple cases (the same could be achieved with a new builder).

Void methods now return `RunResult` object containing `DropwizardTestSupport` object and `Injector` -
everything that could be required for assertions after application run:

```java
RunResult<MyConfig> res = TestSupport.runCoreApp(MyApp.class);

Assertions.assertEquals(2, res.getConfiguration().foo);
```

Shortcut methods with config overrides:

```java
RunResult<MyConfig> res = TestSupport.runCoreApp(MyApp.class,
        "path/to/config.yml",  // could be null
        "foo: 2", "bar: 11");
```

#### Capture console output

There is now a utility to capture console output:

```java
String out = TestSupport.captureOutput(() -> {
        
    // run application inside
    TestSupport.runWebApp(App.class, injector -> {
        ClientSupport client = TestSupport.getContextClient();
        
        // call application api endpoint
        client.get("sample/get", null);

        return null;
    });
});

// uses assert4j, test that client was called (just an example) 
Assertions.assertThat(out)
    .contains("[Client action]---------------------------------------------{");
```

Returned output contains both `System.out` and `System.err` - same as it would be seen in console.

All output is also printed into console to simplify visual validation

!!! warning
    Such tests could not be run in parallel (due to system io overrides)

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

    ```java
    Assertions.assertFalse(result.isSuccessful());  
    Assertions.assertEquals("Error message", result.getException().getMessage());
    ```

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
    All output is always printed to console, so you could always see it after test execution
    (without additional actions)

Commands requiring user input could also be tested (with mocked input):

```java
CommandResult result = TestSupport.buildCommandRunner(App.class)
        .consoleInputs("1", "two", "something else")
        .run("quiz")
```

At least, the required number of answers must be provided (otherwise error would be thrown,
indicating not enough inputs)

!!! warning
    Due to IO overrides, command tests could not run in parallel.   
    For junit 5, such tests could be annotated with [`@Isolated`](https://junit.org/junit5/docs/current/user-guide/#writing-tests-parallel-execution-synchronization)
    (to prevent execution in parallel with other tests)

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
    Config file should not be specified in command itself - builder would add it, if required.  
    But still, it would not be a mistake to use config file directly in command:

    ```java
    TestSupport.buildCommandRunner(App.class)
        // note .config("...") was not used (otherwise two files would appear)!
        .run("cfg", "path/to/config.yml");
    ```

    Using builder for config file configuration assumed to be a preferred way.

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

#### Test application startup fail

Command runner could also be used for application startup fail tests:

```java
CommandResult result = TestSupport.buildCommandRunner(App.class)
        .run("server")
```

or with the shortcut:

```java
CommandResult result = TestSupport.buildCommandRunner(App.class)
        .runApp()
```

!!! note
    In case of application *successful* start, special check would immediately stop it
    by throwing exception (resulting object would contain it), so such test would never freeze.

No additional mocks or extensions required because running like this would not cause
`System.exist(1)` call, performed in `Application` class (see `Application.onFatalError`).