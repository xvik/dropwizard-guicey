# Testing startup error

!!! warning
    Commands execution overrides System IO and so can't run in parallel with other tests!

    Use [`@Isolated`](https://junit.org/junit5/docs/current/user-guide/#writing-tests-parallel-execution-synchronization) 
    on such tests to prevent parallel execution with other tests

Tests for application startup fail often required to check some startup conditions.
The problem is that it's not enough to simply run the application with "bad" configuration file
because on error application calls `System.exit(1)`:

```java
    public abstract class Application<T extends Configuration> {
        ...
    protected void onFatalError(Throwable t) {
        System.exit(1);
    }
}
```

Instead, you can use command run utility:

```java
CommandResult result = TestSupport.buildCommandRunner(App.class)
        .runApp()
```

or with the shortcut:

```java
CommandResult result = TestSupport.buildCommandRunner(App.class)
        .runApp()
```

!!! tip
    [Test framework-agnostic utilities](../general/general.md) provides simple utilities to run application
    (core or web). Could be useful when testing several applications interaction. 

!!! important
    In case of application *successful* start, special check would immediately stop it
    by throwing exception (resulting object would contain it), so such test would never freeze.

