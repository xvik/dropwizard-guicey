# Testing commands

`CommandTestSupport` object is a commands test utility equivalent to `DropwizardTestSupport`.
It uses dropwizard `Cli` for arguments recognition and command selection.

The main difference with `DropwizardTestSupport` is that command execution is
a short-lived process and all assertions are possible only *after* the execution.
That's why command runner would include in the result all possible dropwizard objects,
created during execution (because it would be impossible to reference them after execution).

New builder (almost the same as application execution builder) simplify commands execution:

```java
CommandResult result = TestSupport.buildCommandRunner(App.class)
        .run("simple", "-u", "user")

Assertions.assertTrue(result.isSuccessful());
```

This runner could be used to run *any* command type (simple, configured, environment).
The type of command would define what objects would be present ofter the command execution
(for example, `Injector` would be available only for `EnvironmentCommand`).

Run command arguments are the same as real command arguments (the same `Cli` used for commands parsing).
You can only omit configuration path and use builder instead:

```java
    CommandResult result = TestSupport.buildCommandRunner(App.class)
            .config("path/to/config.yml")
            .configOverride("prop: 1")
            .run("cmd", "-p", "param");
```

!!! important
    Such run *never fails* with an exception: any appeared exception would be
    stored inside the response:

    ```java
    Assertions.assertFalse(result.isSuccessful());  
    Assertions.assertEquals("Error message", result.getException().getMessage());
    ```

### IO

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

### Configuration

Configuration options are the same as in run builder. For example:

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

### Listener

There is a simple listener support (like in application run builder) for setup-cleanup actions:

```java
TestSupport.buildCommandRunner(App.class)
        .listen(new CommandRunBuilder.CommandListener<>() {
            public void setup(String[] args) { ... }
            public void cleanup(CommandResult<TestConfiguration> result) { ... }
        })
        .run("cmd")
```
