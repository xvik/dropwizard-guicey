### Dropwizard-jobs example

Example show [dropwizard-jobs](https://github.com/dropwizard-jobs/dropwizard-jobs) 3rd party library integration.

NOTE: this is very basic integration. Normally it deserves special ext module, but it's not yet exists.

#### Setup

Add dropwizard-jobs dependency:

```groovy
dependencies {
    implementation 'io.github.dropwizard-jobs:dropwizard-jobs-guice:4.0.0-RELEASE'
}
```

#### Integration

App configuration class must implement JobsConfiguration:

```java
public class JobsAppConfiguration extends Configuration implements JobConfiguration
```

Library already provides guice integration, but it couldn't be used directly as it requires immediate
injector presence. Instead, we will use two guicey extensions. 

Note that application will search and install extensions using classpath scan: `.enableAutoConfig(JobsApplication.class.getPackage().getName())`

First we define Managed to manage scheduler lifecycle: 

```java
@Singleton
public class JobsManager extends GuiceJobManager {

    @Inject
    public JobsManager(Injector injector, JobsAppConfiguration configuration) {
        super(configuration, injector);
    }
}
```

It is recognized as Managed and installed automatically. Internally it will lookup all Job bindings in guice context
and register all found jobs.

To avoid manual jobs registration we will use custom installer:

```java
public class JobsInstaller implements FeatureInstaller, TypeInstaller<Job> {

    private final Reporter reporter = new Reporter(JobsInstaller.class, "jobs =");

    @Override
    public boolean matches(Class<?> type) {
        return FeatureUtils.is(type, Job.class);
    }

    @Override
    public void install(Environment environment, Class<Job> type) {
        // here we can also look for class annotations and show more info in console
        // (omitted for simplicity)
        reporter.line("(%s)", type.getName());
    }

    @Override
    public void report() {
        reporter.report();
    }
}
```

It will be recognized and registered automatically. Installer performs two tasks: find job beans and bind to guice context (implicitly)
and print all found jobs to console.

And the final step is configure dopwizard metrics registry in the application class:

```java
@Override
public void initialize(Bootstrap<JobsAppConfiguration> bootstrap) {
    bootstrap.addBundle(GuiceBundle.builder()
            .enableAutoConfig(JobsApplication.class.getPackage().getName())
            .build());

    // force dropwizard-jobs using main metrics registry for all jobs
    SharedMetricRegistries.add(Job.DROPWIZARD_JOBS_KEY, bootstrap.getMetricRegistry());
}
```

#### Sample job

Now we can just declare jobs:

```java
@Singleton
@Every("1s")
public class SampleJob extends Job {

    @Override
    public void doJob(JobExecutionContext context) throws JobExecutionException {
        ...
    }
}
```
