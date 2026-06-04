# Task installer

!!! summary ""
    CoreInstallersBundle / [TaskInstaller](https://github.com/xvik/dropwizard-guicey/tree/master/dropwizard-guicey/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/TaskInstaller.java)

Installs [Dropwizard tasks](https://www.dropwizard.io/en/release-5.0.x/manual/core.html#tasks).

## Recognition

Detects classes extending Dropwizard `#!java Task` and registers their instances in the environment.

```java
public class MyTask extends Task {
    
    @Inject
    private MyService service;
    
    public TruncateDatabaseTask() {
        super("mytask");
    }

    @Override
    public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output) throws Exception {
        service.doSomething();
    }
}
```

Task can be triggered with: `http://localhost:8081/tasks/mytask`
