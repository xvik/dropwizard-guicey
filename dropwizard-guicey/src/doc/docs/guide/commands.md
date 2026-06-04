# Dropwizard commands support

## Guice injections

Guicey calls `injector.injectMembers(command)` for all registered `EnvironmentCommand`s, so you can inject Guice beans directly:

```java
public class MyCommand extends EnvironmentCommand<MyConfiguration> {
    
    @Inject
    private MyService myservice;
    
    public MyCommand(Application application) {
        super(application, "mycli", "my super useful cli");
    }
    
    @Override
        protected void run(Environment environment, 
                             Namespace namespace, 
                             MyConfiguration configuration) throws Exception { 
            myservice.doSomething();        
        }
}
```

!!! note
    It doesn't matter if a command was registered manually, by some bundle, or with command search (see below).

!!! warning
    You can use Guice injections only in `EnvironmentCommand`s because only these commands start bundles (and so launch Guice context creation).

## Automatic installation

Automatic scanning for commands is disabled by default. It can be enabled by:

```java
GuiceBundle.builder()
    .enableAutoConfig("package.to.scan")
    .searchCommands()
```

When enabled, all classes extending `Command` are instantiated using the default constructor and registered in the Dropwizard bootstrap object.

### Simple commands

For example, if the command below is inside a scanned package, then Guicey will automatically register it.

```java
public class MyCommand extends Command {
    
    public MyCommand() {
        super("hello", "Prints a greeting");
    }

    @Override
    public void run(Bootstrap<?> bootstrap, Namespace namespace) throws Exception {
        System.out.println("Hello world");
    }
}
```

### Environment commands

!!! important
    `EnvironmentCommand` must have a constructor with an `Application` argument.

```java
public class SyncCommand extends EnvironmentCommand<AppConfiguration> {

    @Inject
    private RemoteSynchronizer synchronizer;
    @Inject
    private DbManager manager;

    public SyncCommand(Application<AppConfiguration> application) {
        super(application, "sync", "Perform remote synchronization");
    }

    @Override
    protected void run(Environment environment, 
                        Namespace namespace, 
                        AppConfiguration configuration) throws Exception {
        manager.start();
        try {
            synchronizer.synchronize();
        } finally {
            manager.stop();
        }
    }
}
```

This example shows a workaround for managed initialization in commands: `DbManager` is a `Managed` bean that would run automatically
in server mode. But commands never start managed objects, so we have to manually start and stop them.
