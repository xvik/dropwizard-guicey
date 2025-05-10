
# Testing startup fails

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

!!! important
    In case of application *successful* start, special check would immediately stop it
    by throwing exception (resulting object would contain it), so such test would never freeze.

!!! note "Why not run directly?"
    You can run command directly: `new App().run("server")`
    But, if application throws exception in *run* phase, `System.exit(1)` would be called:

    ```java
    public abstract class Application<T extends Configuration> {
        ...
        protected void onFatalError(Throwable t) {
            System.exit(1);
        }
    }
    ```   
    Commands runner runs commands directly so exit would not be called. 
