# 7.2.0 Release Notes

The release contains:
* Many improvements simplifying usage (main bundle, guicey bundles, shared state).
* New reports for measuring application startup performance.
* Private guice modules support
* A lot of test improvements (mostly for junit 5)

## Un-deprecated HK2 support

All deprecation annotations removed for HK2 related features. Soft deprecation remain in javadoc.

It is possible to replace HK2 context with a child guice injector (conceptual prototype was ready long ago).
But it requires A LOT of time to finish. Eventually, it would be finished, but, for now,
other features are in priority.

Hard deprecation removed because there are no alternatives (and will not appear soon).
Sorry for the inconvenience.

## GuiceBundle builder

### Configuration awareness 

Before, `GuiceyBundle` was required to perform any registrations, requiring configuration.
Now there is an alternative:

```java
GuiceBundle.builder()
   ...
   .whenConfigurationReady(env -> {
        // env is GuiceyEnvironment, like in GuiceyBundle#run
        AppConfig config = env.configuration();
        env.modules(new GuiceModule(config));
   });
```

### Listener shortcuts

Listener shortcut methods added (same as in `GuiceyEnvironment`):

```java
GuiceBundle.builder()
    ...
    // these listeners are shortcuts of guicey listeners (.listen())
    // guice injector created (dropwizard run phase)
    .onGuiceyStartup((config, env, injector) -> {
        // just an example - there could be loginc, usually declared 
        // in application run method
        env.jersey().register(injector.getInstance(SomeBean.class));
     })
    // executes after complete application startup (including guicey lightweight test)   
    .onApplicationStartup(injector -> ...)
    //executes after application shutdown
    .onApplicationShutdown(injector -> ...)

    // these methods just for convenience: 
    // they actually use .onGuiceyStartup() to register provided listeners
    .listenServer(server -> ...) 
    .listenJetty(new LifeCycle.Listener() {...})
    .listenJersey(new ApplicationEventListener() {...})
```

!!! note
    All the same listeners could be registered within `.whenConfigurationReady(env -> env.listenServer(...)`,
    but still they remain in the main bundle for more clarity (simpler to find, simpler to use in some cases)

Also, note that all these methods are available now for `GuiceyConfigurationHook`.
For example, it could be used in tests:

```java
    @EnableHook
    static GuiceyConfigurationHook hook = builder -> builder
            .printStartupTime()
            .onGuiceyStartup((config, env, injector) -> env.lifecycle().manage(...));
```

## Diagnostic reports

### Application startup time report

The new report intended to show the entire application startup time information to simplify
searching for bottlenecks. It's hard to measure everything exactly from a bundle,
but the report will try to show the time spent in each phase (init, run, web) and time of each
registered dropwizard bundle.

```java
GuiceBundle.builder()
    .printStartupTime()
```

Sample output:

```
INFO  [2025-03-27 09:12:27,435] ru.vyarus.dropwizard.guice.debug.StartupTimeDiagnostic: Application startup time: 

	JVM time before                    : 1055 ms

	Application startup                : 807 ms
		Dropwizard initialization          : 127 ms
			GuiceBundle                        : 123 ms (finished since start at 127 ms)
				Bundle builder time                : 38 ms
				Hooks processing                   : 3.23 ms
					StartupDiagnosticTest$Test1$$Lambda/0x0000711de72a1d70: 2.37 ms
				Classpath scan                     : 44 ms
				Commands processing                : 4.41 ms
					DummyCommand                       : 0.42 ms
					NonInjactableCommand               : 3.16 ms
				Bundles lookup                     : 1.15 ms
				Guicey bundles init                : 3.24 ms
					WebInstallersBundle                : 0.52 ms
					CoreInstallersBundle               : 1.83 ms
				Installers time                    : 21 ms
					Installers resolution              : 15 ms
					Scanned extensions recognition     : 6.13 ms
				Listeners time                     : 1.35 ms
					ConfigurationHooksProcessedEvent   : 0.23 ms
					BeforeInitEvent                    : 0.59 ms
					BundlesResolvedEvent               : 0.009 ms
					BundlesInitializedEvent            : 0.43 ms
					CommandsResolvedEvent              : 0.006 ms
					InstallersResolvedEvent            : 0.01 ms
					ClasspathExtensionsResolvedEvent   : 0.009 ms
					InitializedEvent                   : 0.007 ms

		Dropwizard run                     : 679 ms
			Configuration and Environment      : 483 ms
			GuiceBundle                        : 196 ms
				Configuration analysis             : 20 ms
		...		
```

!!! note "Limitations"
* Can't show init time of dropwizard bundles, registered before the guice bundle (obviously)
* `Applicaion#run` method time measured as part of "web" (the bundle can't see this point, but should not be a problem)

The report could be also enabled for compiled application: `-Dguicey.hooks=startup-time` 

### Guice provision time

The new report intended to show the time of guice beans provision (instance construction, 
including provider or provider method time). It shows all requested guice beans and the 
number of obtained instances (for prototype scopes). 

```java
GuiceBundle.builder()
    .printGuiceProvisionTime()
```

All provisions are sorted by time:

```
INFO  [2025-03-27 09:20:32,313] ru.vyarus.dropwizard.guice.debug.GuiceProvisionDiagnostic: Guice bindings provision time: 

	Overall 57 provisions took 1.40 ms
		binding              [@Singleton]     ManagedFilterPipeline                                                                 : 0.88 ms    		 com.google.inject.servlet.InternalServletModule.configure(InternalServletModule.java:94)
		binding              [@Singleton]     ManagedServletPipeline                                                                : 0.45 ms    		 com.google.inject.servlet.InternalServletModule.configure(InternalServletModule.java:95)
		providerinstance     [@Singleton]     @ScopingOnly GuiceFilter                                                              : 0.02 ms    		 com.google.inject.servlet.InternalServletModule.provideScopingOnlyGuiceFilter(InternalServletModule.java:106)
		JIT                  [@Prototype]     JitService                                                                       x10  : 0.02 ms (0.006 ms + 0.002 ms + 0.001 ms + 0.001 ms + 0.001 ms + ...) 		 
		binding              [@Singleton]     GuiceyConfigurationInfo                                                               : 0.01 ms    		 ru.vyarus.dropwizard.guice.module.GuiceBootstrapModule.configure(GuiceBootstrapModule.java:63)
		binding              [@Singleton]     BackwardsCompatibleServletContextProvider                                             : 0.007 ms   		 com.google.inject.servlet.InternalServletModule.configure(InternalServletModule.java:99)
		instance             [@Singleton]     Bootstrap                                                                             : 0.004 ms   		 ru.vyarus.dropwizard.guice.module.GuiceBootstrapModule.bindEnvironment(GuiceBootstrapModule.java:71)
		instance             [@Singleton]     @Config("server.gzip.minimumEntitySize") DataSize                                     : 0.002 ms   		 ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:129)
		instance             [@Singleton]     Environment                                                                           : 0.0009 ms  		 ru.vyarus.dropwizard.guice.module.GuiceBootstrapModule.bindEnvironment(GuiceBootstrapModule.java:72)
		instance             [@Singleton]     @Config AdminFactory                                                                  : 0.0008 ms  		 ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindUniqueSubConfigurations(ConfigBindingModule.java:117)
		...
```

The report will also try to detect injection mistakes in case when JIT (just in time) binding is used
when there are qualified declarations with the same type. 

The most common mistake is configuration objects misuse: guicey binds unique configuration objects 
with `@Config` qualifier, but, if injection point declared without the qualifier,
guice will create a JIT binding (create new object instance) instead of injecting
declared instance. This might be hard to spot, especially when lombok is used (which may not
copy field annotation into constructor).

```java
INFO  [2025-03-27 09:21:33,438] ru.vyarus.dropwizard.guice.debug.GuiceProvisionDiagnostic: Guice bindings provision time: 

	Possible mistakes (unqualified JIT bindings):

		 @Inject Sub:
			  instance             [@Singleton]     @Config("val2") Sub                                                                   : 0.0005 ms  		 ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:129)
			  instance             [@Singleton]     @Marker Sub                                                                           : 0.0007 ms  		 ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindCustomQualifiers(ConfigBindingModule.java:87)
			> JIT                  [@Prototype]     Sub                                                                                   : 0.006 ms   		 

		 @Inject Uniq:
			  instance             [@Singleton]     @Config Uniq                                                                          : 0.0005 ms  		 ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindUniqueSubConfigurations(ConfigBindingModule.java:117)
			> JIT                  [@Prototype]     Uniq                                                                                  : 0.004 ms   		 

	Overall 53 provisions took 1.45 ms
		binding              [@Singleton]     ManagedFilterPipeline                                                                 : 0.78 ms    		 com.google.inject.servlet.InternalServletModule.configure(InternalServletModule.java:94)
		binding              [@Singleton]     ManagedServletPipeline                                                                : 0.44 ms    		 com.google.inject.servlet.InternalServletModule.configure(InternalServletModule.java:95)
```

In this example, the report detects incorrect injections:

```java
   @Inject
   private Sub val;
   @Inject
   private Uniq uniq;
```

Detection will also work for generified bindings:

```
	Possible mistakes (unqualified JIT bindings):

		 @Inject Service:
			  instance             [@Singleton]     Service<Integer>                                                                      : 0.0006 ms  		 ru.vyarus.dropwizard.guice.debug.provision.GenerifiedBindingsTest$App.lambda$configure$0(GenerifiedBindingsTest.java:46)
			  instance             [@Singleton]     Service<String>                                                                       : 0.002 ms   		 ru.vyarus.dropwizard.guice.debug.provision.GenerifiedBindingsTest$App.lambda$configure$0(GenerifiedBindingsTest.java:45)
			> JIT                  [@Prototype]     Service                                                                               : 0.004 ms   		 

```

The report could be also enabled for compiled application: `-Dguicey.hooks=provision-time`

The report shows only provisions performed in application startup, but it could be used in 
tests to detect provision problems at runtime:

```java
    @EnableHook
    static GuiceProvisionTimeHook report = new GuiceProvisionTimeHook();

@Test
void testRuntimeReport() {
    // clear startup data
    report.clearData();
    // do something that might cause additional provisions
    injector.getInstance(Service.class);
    injector.getInstance(Service.class);

    // assert
    Assertions.assertThat(report.getRecordedData().keys().size()).isEqualTo(2);
    // or just print report (only for recorded provisions)
    System.out.println(report.renderReport());
}   
```

### Shared state report

Guicey shared state is a bundle communication mechanism and safe "static" access 
for the important objects (quite rarely required). Before, it was not clear the real sequence of state 
population and access, and now there is a special report showing all state manipulations:

```java
GuiceBundle.builder()
    .printSharedStateUsage()
```

```
INFO  [2025-03-27 09:49:35,219] ru.vyarus.dropwizard.guice.debug.SharedStateDiagnostic: Shared configuration state usage: 

	SET Options (ru.vyarus.dropwizard.guice.module.context.option)                      	 at r.v.d.g.m.context.(ConfigurationContext.java:167)

	SET List (java.util)                                                                	 at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:60)
		MISS at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:56)
		GET at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:57)
		GET  at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:60)
		GET  at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:61)
		GET  at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:62)
		GET  at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:73)
		GET  at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:74)
		GET  at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:82)
		GET  at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:84)

	SET Bootstrap (io.dropwizard.core.setup)                                            	 at r.v.d.g.m.context.(ConfigurationContext.java:806)

	SET Map (java.util)                                                                 	 at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:97)
		MISS at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:93)
		GET at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:94)
		GET  at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:97)
		GET  at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:98)
		GET  at r.v.d.g.d.SharedStateDiagnosticTest.(SharedStateDiagnosticTest.java:101)
    ...		
```

### Guice bindings report fixes

Guice bindings report (`.printGuiceBindings()` or `.printAllGuiceBindings()`) was fixed:

* Fixed scope accuracy for linked bindings
* Fixed bindings for private modules (missed exposed linked bindings)

## GuiceyBundle

### Checked initialization exceptions

Added `throws Exception` in bundle init method:

```java
public void initialize(final GuiceyBootstrap bootstrap) throws Exception {
```

!!! important
    This is not a breaking change: all existing bundles will work (even compiled with 
    the previous guicey version). Existing runtime exception is re-thrown so even tests,
    relying on the exception type or message would not break.
    It's even possible to avoid `throws Exception` in new bundles.

Initially, `throws Exception` was not added to comply with dropwizard bundles (`ConfiguredBundle`):
dropwizard assumes only runtime exceptions in initialization phase and any exception 
in run phase.

But, it appears that often it is more useful to allow checked exceptions in init method to
avoid clumsy exception handling (especially for quick prototyping).

### Register extensions at run phase

Now extensions could be registered in run phase:

```java
public class MyBundle implements GuiceyBundle {
    @Override
    public void run(final GuiceyEnvironment environment) throws Exception {
        MyConfig config = environment.configuration();
        if (config.isFeatureRequired()) {
            environment.extensions(SomeExtension.class);
            // OR
            environment.extensionsOptions(SomeExtension.class);
        }
    }
}
```

Initially, it was assumed that all configuration should be done in the configuration phase.
But there are cases when extension registration depends on configuration (or even configuration provides 
additional extensions).

Disabling already registered extensions also appears to not cover all cases.

Also, guicey already detects extensions from guice bindings in the run phase, so it was
already registering extensions in the run phase.

Related internal changes (will not anything, just in case):

* Manually registered extensions now validated in run phase (and so `ManualExtensionsValidatedEvent` called at run phase)
* As before, classpath scan is performed at configuration phase, but actual extensions
  registration appears at run phase (because manual extensions registered in priority)

### Transitive guice bundles order

```java
public class MyBundle implements GuiceyBundle {
    @Override
    public void initialize(final GuiceyBootstrap bootstrap) throws Exception {
        // transitive bundle
        boostrap.bundles(new MySubBundle());
    }
}
```

**Before**, all transitive bundles were initialized after root bundles:
`MySubBundle.initialize()` called after `MyBundle.initialize()` (and all other root bundles).
At run phase bundles were executed in the same sequence: `MyBundle.run()` then `MySubBundle.run()`. 

Usually, initialization order does not matter, but, in some cases, it is important to have
the transitive bundle initialized immediately (for example, it could store some important value 
into shared state).

Also, both dropwizard bundles and guice modules immediately initialize transitives.
To unify behavior, transitive guicey bundles now also **initialize immediately**.

So in example above, `MySubBundle.initialize()` would be called just in time of the
bundle registration `boostrap.bundles(new MySubBundle())` and, after registration 
root bundle could rely on transitive bundle (initialized) state.

At run phase, the transitive bundle would also be called before the root:
`MySubBundle.run()` then `MyBundle.run()`.

### Listeners shortcuts

Add listener registration shortcuts at bundle run:

```java
    @Override
    public void run(final GuiceyEnvironment environment) throws Exception {
        environment
                // executes after application shutdown
                .onApplicationShutdown(injector -> ...)
                // jersey startup events and requests listener
                .listenJersey(new ApplicationEventListener() {...})
    }
```

Just a useful shortcuts.

## Hook

Added `throws Exception` for guicey hook:

```java
public interface GuiceyConfigurationHook {
    void configure(GuiceBundle.Builder builder) throws Exception;
}
```

!!! important
    This is not a breaking change: all existing hooks will work (even compiled with
    the previous guicey version). Existing runtime exception is re-thrown so even tests,
    relying on the exception type or message would not break.
    It's even possible to avoid `throws Exception` in new hooks.

Avoiding checked exception is especially useful when writing test hooks. 

## Private guice modules support

**Before** guicey did not try to search extension inside private guice modules. But this
is not quite correct because if extension is declared in private module and it would
be also registered manually, then guicey would create another binding for it, which may
cause conflicts.

**Now** guicey analyzes private modules too. For example:

```java
public class PModule extends PrivateModule {
    @Override
    protected void configure() {
        // ExtImpl is extension (recognition sign absent in interface) 
        bind(IExt.class).to(ExtImpl.class);
        // extension exposed by interface 
        expose(IExt.class);
    }
}

public interface IExt {... }
public class Ext implements IExt, Managed { ... }
```

Guicey would detect that `ExtImpl` is an extension, and it is available (through exposed interface)
and so register it as an extension.

!!! important
    Guicey rely on extension classes and so it would need direct extension access (to be able to call
    `Injector.getInstance(ExtImpl.class)`). By default, it is not possible (exposed only interface),
    but guicey would change private module: it would add an additional `expose` for `ExtImpl`.

Also, as any guicey extension could be disabled, then `.disable(ExtImpl.class)`
would remove binding inside private module (works only for top-level private modules).

If you'll face any problems with private modules behavior, **please report it**.

Old behavior could be reverted with:

```java
GuiceBundle.builder()
    .option(GuiceyOptions.AnalyzePrivateGuiceModules, false)
```

## Classpath scan

### Extensions scan filters

By default, classpath scanner checks all available classes, and the only way to extension avoid 
recognition is `@InvisibleForScanner` annotation.

Now custom conditions could be specified:

```java
GuiceBundle.builder()
    .autoConfigFilter(ignoreAnnotated(Skip.class))
```

In this example, classes annotated with `@Skip` would not be recognized.

!!! note
    Assumed static import for `ClassFilters` utility, containing the most common cases.
    If required, raw predicate could be used:
    ```java
    .autoConfigFilter(cls -> !cls.isAnnotationPresent(Skip.class))
    ```

It is also possible now to implement spring-like approach when only annotated classes 
are recognized:

```java
GuiceBundle.builder()
    .autoConfigFilter(annotated(Component.class, Service.class))
```

Here only extensions annotated with `@Component` or `@Service` would be recognized.

!!! note
    This filter affects only extension search: installers and commands search does not use filters
    (because it would be confusing and error-prone).

!!! tip
    Multiple filters could be specified:
    ```java
    GuiceBundle.builder()
        .autoConfigFilter(annotated(Component.class, Service.class))
        .autoConfigFilter(ignoreAnnotated(Skip.class))
    ```

Auto config filter also affects extensions recognition from guice bindings and so
could be used for ignoring extensions from bindings.

It is also possible now to exclude some sub-packages from classpath scan:

```java
GuiceBundle.builder()
    .enableAutoConfig("com.company.app")
    .autoConfigFilter(ignorePackages("com.company.app.internal"))
```

### Private extension classes

By default, guicey does not search extensions in protected and package-private classes:

```java
public class Something {
    static class Ext1 implements Managed {}
    protected static class Ext2 implements Managed {}
}
```

Now searching such extensions could be enabled with:

```java
GuiceBundle.builder()
    .option(GuiceyOptions.ScanProtectedClasses, true)
```

## Disable predicate

Disable predicate is useful for disabling a wide range of extensions:

```java
GuiceBundle.builder()
    .disable(Disables.inPackage("com.company.app.rest.stubs"))
```

But, **before** predicate was called too early: before actual installer is assigned
to the extension item model and so it was impossible to disable extensions by installer.

**Now** extensions could be disabled by installer.

For convenience, new installer-related shortcuts added:

* `Disables.jerseyExtension()`
* `Disables.webExtension()` (servlets, filters and jersey)
* `Disables.installedBy(...)`

Example usage:

```java
@EnableHook
static GuiceyConfigurationHook hook = builder ->
        builder.disable(installedBy(WebFilterInstaller.class));
```

Also, disable shortcuts for exact items type (`Disables.module()`, `Disabled.extension()`, etc.) now raise predicate 
type to simplify chained usage:

```java
builder.disable(module().and(ModuleItemInfo mod -> ! mod.isOverriding()));
```

## Shared state

### State key (BREAKING)

Before, it was recommended to use bundle class as a shared state key,
but it appears to be very confusing.

Now **shared state object class must be used as a key**

This affects both state storing:

```java
// before was: put(Bundle.class, new MyState())
state.put(MyState.class, new MyState());
```

And access:

```java
// before was: MyState myState = state.get(Bundle.class);
MyState myState = state.get(MyState.class);
```

This way shared state usage becomes type-safe (impossible to use a wrong type for 
the stored object by mistake).

### Options accessor

Shared state now holds a reference to guicey options values:

```java
SharedConfigurationState.get(environment).get().getOptions()
```

### Reactive access

Added reactive state access method (action called as soon as value would be set).
This is mostly useful for the main guice bundle: 

```java
GuiceBundle.builder()
    withSharedState(state -> 
        state.whenReady(MyState.class, mystate -> ...))
```

`whenReady` action called either immediately, if value already present, or just after value
would be set. Listener calls could be seen on the new shared state report (same as never called listeners)

## Test

### Disable managed lifecycle

Added ability to disable managed objects lifecycle for lightweight guicey tests:

JUnit 5 extensions:

```java
@TestGuiceyApp(.., managedLifecycle = false)
```

```java
@RegisterExtension
static TestGuiceyAppExtension ext = TestGuiceyAppExtension.forApp(..)
        ...
        .disableManagedLifecycle()
```

In this case, start/stop method would not be called for managed objects.
This might be useful for disabling some heavy application initialization ligic,
defined in managed objects, but not required in tests.

!!! note
    Application lifecycle will remain: events like `onApplicationStartup` would still be 
    working (and all registered `LifeCycle` objects would work). Only managed objects ignored.

Option is also available for core (non-junit) testing support:

* `new GuiceyTestSupport().disableManagedLifecycle()`
* `TestSupport.build(App.class).runCoreWithoutManaged(..)`

### Manual configuration objects

It is now possible to manually construct configuration object instance in
junit5 extension (for both lightweight and full app tests):

```java
@RegisterExtension
static TestGuiceyAppExtension ext = TestGuiceyAppExtension.forApp(..)
        .config(() -> new MyConfig())
        ...
```

Or in setup object:

```java
@EnableSetup
static TestEnvironmentSetup setup = ext -> ext.config(() -> new MyConfig())
```

!!! important
    Configuration overrides would not work with manually created configuration objects.
    Use configuration modifiers instead.

### Config override for a single key

Added config override for a single key-value pair (for the setup object and extension builders):

```java
@EnableSetup
static TestEnvironmentSetup setup = ext -> ext.configOverride("some.key", "12");
```

(before, only methods with supplier and multiple keys were available)


### Configuration modifiers

Dropwizard configuration overrides mechanism is limited (for example, it would not work for a collection property).

Configuration modifier is an alternative mechanism when all changes are performed on
configuration instance. 

Modifier could be used as lambda:

```java
@RegisterExtension
static TestGuiceyAppExtension ext = TestGuiceyAppExtension.forApp(..)
        .configModifiers(config -> config.getSomething().setFoo(12))
        ...
```

Or in setup object:

```java
@EnableSetup
static TestEnvironmentSetup setup = ext -> 
        ext.configModifiers(config -> config.getSomething().setFoo(12))
```

Modifier could be declared in class:

```java
public class MyModifier implements ConfigModifier<MyConfig> {
    @Override
    public void modify(MyConfig config) throws Exception {
        config.getSomething().setFoo(12);
    }
}

@TestGuiceyApp(.., configModifiers = MyModifier.class)
```

!!! tip
    Modifier could be used with both manual configuration or usual (yaml) configuration.
    Configuration modifiers also could be used together with configuration overrides.

!!! warning "Limitation" 
    Configuration modifiers are called after dropwizard logging configuration,
    so logging is the only thing that can't be configured (use configuration overrides for logging)

Configuration overrides are also available in core test extensions:

* Commands runner: `TestSupport.buildCommandRunner(..).configModifiers(...)` 
* Raw test support builder: `TestSupport.build(..).configModifiers(...)`
* `GuiceyTestSupport.configModifiers(..)`
* For `DropwizardTestSupport` custom command must be used to support modifiers: 
    `ConfigOverrideUtils.buildCommandFactory`

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

### Debug option evolution

Existing junit extensions debug mechanism was evolved: 

```java
@TestGuiceyApp(.., debug = true)
```

#### Extensions time

To simplify slow tests (slowness) investigations, guicey now measures and prints extensions time.

For example, test with application started in beforeAll, with two test methods
(same app for both tests):

```java
@TestGuiceyApp(value = App.class, debug = true)
public class PerformanceLogTest {
    @Test
    public void test1() { ... }
    @Test
    public void test2() { ... }
}
```

```
\\\------------------------------------------------------------/ test instance = 1595d2b2 /
Guicey time after [Before each] of PerformanceLogTest#test1(): 1204 ms

	[Before all]                       : 1204 ms
		Guicey fields search               : 2.03 ms
		Guicey hooks registration          : 0.02 ms
		Guicey setup objects execution     : 1.92 ms
		DropwizardTestSupport creation     : 1.47 ms
		Application start                  : 1172 ms

	[Before each]                      : 0.46 ms
		Guice fields injection             : 0.19 ms


\\\------------------------------------------------------------/ test instance = 45554613 /
Guicey time after [Before each] of PerformanceLogTest#test2(): 1205 ms ( + 0.33 ms)

	[Before each]                      : 0.69 ms ( + 0.23 ms)
		Guice fields injection             : 0.36 ms ( + 0.17 ms)

	[After each]                       : 0.10 ms


\\\---------------------------------------------------------------------------------------------
Guicey time after [After all] of PerformanceLogTest: 1207 ms ( + 2.15 ms)

	[After each]                       : 0.11 ms ( + 0.01 ms)

	[After all]                        : 2.14 ms
		Application stop                   : 1.72 ms
```

There are three reports:
 
1. Before first test method (see guicey extension startup time)
2. Before the second test method (see guicey time for the second method only)
3. After all (cleanup time)

Only the first report shows all recorded times, next reports only mention time increase.
For example, the second report mentions only `Guice fields injection             : 0.36 ms ( + 0.17 ms)`
Meaning guicey perform fields injection just before the second test, spent 0.17 ms on it 
(overall injection time for two injections is 0.36 ms) 

#### Updated declarations report

All declared setup objects and hooks now showed with a (declaration) source reference (where possible).
Simplified report for lambdas (was not very readable before).

```java
public static class Test2 extends Base {

    @RegisterExtension
    static TestGuiceyAppExtension app = TestGuiceyAppExtension.forApp(App.class)
            .setup(Ext1.class, Ext2.class)
            .setup(it -> null, new Ext3())
            .debug()
            .create();

    @EnableSetup
    static TestEnvironmentSetup ext1 = it -> null;
    @EnableSetup
    static TestEnvironmentSetup ext2 = it -> null;
```

```
Guicey test extensions (Test2.):

	Setup objects = 
		Ext1                           	@RegisterExtension.setup(class)                    at r.v.d.g.t.j.d.SetupObjectsLogTest.(SetupObjectsLogTest.java:102)
		Ext2                           	@RegisterExtension.setup(class)                    at r.v.d.g.t.j.d.SetupObjectsLogTest.(SetupObjectsLogTest.java:102)
		<lambda>                       	@RegisterExtension.setup(obj)                      at r.v.d.g.t.j.d.SetupObjectsLogTest.(SetupObjectsLogTest.java:103)
		Ext3                           	@RegisterExtension.setup(obj)                      at r.v.d.g.t.j.d.SetupObjectsLogTest.(SetupObjectsLogTest.java:103)
		<lambda>                       	@EnableSetup Base#base1                            at r.v.d.g.t.j.d.SetupObjectsLogTest$Base#base1
		<lambda>                       	@EnableSetup Base#base2                            at r.v.d.g.t.j.d.SetupObjectsLogTest$Base#base2
		<lambda>                       	@EnableSetup Test2#ext1                            at r.v.d.g.t.j.d.SetupObjectsLogTest$Test2#ext1
		<lambda>                       	@EnableSetup Test2#ext2                            at r.v.d.g.t.j.d.SetupObjectsLogTest$Test2#ext2
```

### Inject test fields once

By default, guicey would inject test field values before every test method, even if the same
test instance used (`TestInstance.Lifecycle.PER_CLASS`). This should not be a problem
in the majority of cases because guice injection takes very little time.
Also, it is important for prototype beans, which will be refreshed for each test.

Now it is possible to inject fields just once:

```java
@TestGuiceyApp(value = App.class, injectOnce = true)
// by default new test instance used for each method, so injectOnce option would be useless 
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PerClassInjectOnceGuiceyTest {
    @Inject
    Bean bean;
    
    @Test
    public test1() {..}

    @Test
    public test2() {..}
}
```

In this case, the same test instance used for both methods (`Lifecycle.PER_CLASS`)
and `Bean bean` field would be injected just once (`injectOnce = true`)

!!! tip
    To check the actual fields injection time enable debug (`debug = true`) and
    it will print injection time before each test method:
    ```
    [Before each]                      : 2.05 ms
        Guice fields injection             : 1.58 ms    
    ```

### Setup objects evolution

Added `throws Exception` in setup object method:

```java
public class GuiceyEnvironmentSetup {
    public Object setup(TestExtension extension) throws Exception { }
}
```

!!! important
    This is not a breaking change: all existing setup objects will work (even compiled with
    the previous guicey version). Existing runtime exception is re-thrown so even tests,
    relying on the exception type or message would not break.
    It's even possible to avoid `throws Exception` in new objects.

With all extensions below, setup objects could completely replace some native junit extensions:
meaning it would be simpler now to implement just a setup object instead of separate junit 
extension implementation.

#### Junit context

Junit context is now directly accessible in builder:

```java
public class MyExt implements GuiceyEnvironmentSetup {
    @Override
    public Object setup(TestExtension extension) throws Exception {
        ExtensionContext context = extension.getJunitContext();
    }
}
```

#### Lifecycle listeners

Added lifecycle listener interface, representing junit phases (before/after) and tested 
application (started/stopped):

```java
public interface TestExecutionListener {
    default void started(final EventContext context) throws Exception {}
    default void beforeAll(final EventContext context) throws Exception {}
    default void beforeEach(final EventContext context) throws Exception {}
    default void afterEach(final EventContext context) throws Exception {}
    default void afterAll(final EventContext context) throws Exception {}
    default void stopped(final EventContext context) throws Exception {}
}
```

`EventContext` provides access for guice injector, DropwizardTestSupport object and junit 5 context.

Raw listener is useful for large setup objects:

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

For field declarations (lambda-based), special individual shortcuts are available:

```java
@EnableSetup
static TestEnvironmentSetup setup = ext -> ext
        .onApplicationStart(event -> ...)
        .onBeforeAll(event -> ...)
        .onBeforeEach(event -> ...)
        .onAfterEach(event -> ...)
        .onAfterAll(event -> ...)
        .onApplicationStop(event -> ...)
```

#### Extension debug

For simplicity, setup object could re-use guicey extension debug trigger to 
print additional data:

```java
@TestGuiceyApp(value = App.class, stup = MySetup.class, debug = true)

public class MySetup implements TestEnvironmentSetup, TestExecutionListener {
    @Override
    public Object setup(TestExtension extension) throws Exception {
        extension.listen(this);
        
        if (extension.isDebug()) {
            System.out.println("Debug info: ...");
        }
    }

    @Override
    public void started(final EventContext context) throws Exception {
        if (context.isDebug()) {
            System.out.println("Debug info: ...");
        }
    }
```

### Annotated fields search API

Added `.findAnnotatedFields()` method to search annotated test fields.

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

Returned object is also abstraction: `AnnotatedField` - it simplifies working with filed value,
plus contains additional checks. 

Added `AnnotatedTestFieldSetup` base class which implements base fields workflow
(including proper nested tests support and guice bindings override (if required)).

All new field extensions (see below) are using this base class. Using them as 
implementation examples, it should be pretty simple to add other field-based test
extensions.

### Auto lookup

Custom `TestEnvironmentSetup` objects could be loaded automatically now 
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

### New test extensions

All new extensions are enabled by default: if any of them will cause any problems, 
please report. New extensions could be disabled with (this will also switch off 
an extension lookup mechanism) 

```java
@TestGuiceyApp(.., useDefaultExtensions = false)
```

After disabling, some extensions could be registered manually (they are all just setup objects). 

!!! tip
    All recognized extension fields would be printed to console when guicey extension debug 
    is enabled (`debug = true`).

New extensions:

* Stubs (with deep injection)
* Mockito mocks (with deep injection)
* Mockito spies 
* Guice bean calls tracker (method arguments and return value captor + time measure)
* Logs test support
* Lightweight REST tests support

#### Stubs

Stub is a (manual) replacement for the real bean. 
For example, suppose application has a `BillingService`, but for tests you 
want to replace it with a simple (no-external-communication) implementation:
`StubBillingService extends BillingService`.

Stubs declared in test class with a new `@StubBean` annotation:

```java
@TestGuiceyApp
public class Test {
    
    @StubBean(BillingService.class)
    StubBillingService stub;
}
```

In this example, stub is instantiated with guice (but you could do it manually) and 
override `BillingService` injection in guice context (using guice modules override).
That means that stub would not only be available in the annotated field, but all guice 
beans, requiring `BillingService` would actually receive stub instance instead of it.

If you try to declare service injection in test:

```java
@Inject
BillingService billingService;
```

You'll see that it's the same instance as a stub.


!!! note
    As a stub object injected into guice context, which by default used "per class",
    the same stub instance would be used for all methods in test (of course, if guice extension
    not declared "per method"). A stub object could implement special interface `StubLifecycle`
    (with before/after methods) which would be called before and after each test method
    (could be used to reset state).

#### Mocks

Requires mockito dependency (version not required if dropwizard (or guicey) BOM used):

```groovy
testImplementation 'org.mockito:mockito-core'
```

Essentially, mocks are automatic stubs with the ability to dynamically declare method behavior.

Mocks declared with a `@MockBean` annotation:

```java
@TestGuiceyApp(...)
public class Test {
    
    @MockBean
    MyService mock;

    @BeforeEach
    void setUp() {
        when(mock.foo()).thenReturn("something");
    }
}
```

Mock instance created automatically (`Mockito.mock(MyService.class)`) and override
`MyService` declaration (using guice modules override).

!!! important
    The main difference with mockito junit extension is that mock instance would be injected
    in all places using mocked service (replace real service in the entire application)

Mock behavior could be declared in test setup method or just before usage in test method.

If you try to declare service injection in test:

```java
@Inject
MyService service;
```

You'll see that it's the same instance as a mock.

!!! note
    As a mock object injected into guice context, which by default used "per class",
    the same stub instance would be used for all methods in test (of course, if guice extension
    not declared "per method"). A mock would be reset (`Mockito.reset(mock)`) automatically
    after each test method.

Mockito provides a mock usage report (`Mockito.mockingDetails(value).printInvocations()`), 
which could be enabled with `@MockBean(printSummary = true)` (report shown after each test method):

```
\\\------------------------------------------------------------/ test instance = 6d420cdd /
@MockBean stats on [After each] for MockSummaryTest$Test1#test():

	[Mockito] Interactions of: Mock for Service, hashCode: 1340267778
	 1. service.foo(1);
	  -> at ru.vyarus.dropwizard.guice.test.jupiter.setup.mock.MockSummaryTest$Test1.test(MockSummaryTest.java:55)
	   - stubbed -> at ru.vyarus.dropwizard.guice.test.jupiter.setup.mock.MockSummaryTest$Test1.setUp(MockSummaryTest.java:50)
```

#### Spies

Requires mockito dependency (version not required if dropwizard (or guicey) BOM used):

```groovy
testImplementation 'org.mockito:mockito-core'
```

Spy is a partial mock: real service instance is proxied. Spies could be used to track
instance calls or to modify some method behavior.

Spies declared with a `@SpyBean` annotation:

```java
@TestGuiceyApp(...)
public class Test {
    
    @SpyBean
    MyService spy;
    
    // just for example (not required)
    @Inject 
    MyService service;

    // optional
    @BeforeEach
    void setUp() {
        // important declaration reversed (otherwise real method is called during declaration)! 
        doReturn(12).when(spy).foo();
    }
    
    @Test
    public void test() {
        // calling methods on injected service to show that it is also a spy
        // (normally spies would be called indirectly (by other beans))
        Assertions.assertEquals(12, service.foo());
        Assertions.assertEquals(1, service.bar(1));

        // foo() (mocked method) called once
        Mockito.verify(spy, Mockito.times(1)).foo();
        // bar(1) called once 
        Mockito.verify(spy, Mockito.times(1)).bar(1);
    }
}
```

Spy instance created automatically (`Mockito.spy(service)`) and override
`MyService` declaration (using guice AOP).

In the example above, method `foo()` is mocked and method `bar(..)` is not (original service method called).

!!! important "Limitation"
    Spy objects would **work only for beans, created by guice**. 
    Spy creation requires original service instance, and so guice AOP used to 
    intercept call the original bean, create spy dynamically, and redirect calls
    to the spy object.

!!! note
    As a spy object tied with guice context, which by default used "per class",
    the same spy instance would be used for all methods in test (of course, if guice extension
    not declared "per method"). A spy would be reset (`Mockito.reset(mock)`) automatically
    after each test method.

Same as for mocks, a usage report could be printed after each test `@SpyBean(printSummary = true)`

```
\\\------------------------------------------------------------/ test instance = 285bf5ac /
@SpyBean stats on [After each] for SpySummaryTest$Test1#test():

	[Mockito] Interactions of: ru.vyarus.dropwizard.guice.test.jupiter.setup.spy.SpySummaryTest$Service$$EnhancerByGuice$$60e90c@40fe8fd5
	 1. spySummaryTest$Service$$EnhancerByGuice$$60e90c.foo(
	    1
	);
	  -> at ru.vyarus.dropwizard.guice.test.jupiter.setup.spy.SpySummaryTest$Test1.test(SpySummaryTest.java:50)
```

!!! tip
    Spies (in some cases) are not very easy to use for tracking method arguments and return values.
    `@TrackBean` extension could be more useful for actual values tracking
    (could be used together with spies or mocks). 

    Example of how to intercept method call result:
    ```java
    public static class ResultCaptor<T> implements Answer {
        private T result = null;
        public T getResult() {
            return result;
        }

        @Override
        public T answer(InvocationOnMock invocationOnMock) throws Throwable {
            result = (T) invocationOnMock.callRealMethod();
            return result;
        }
    }

    ResultCaptor<String> resultCaptor = new ResultCaptor<>();
    Mockito.doAnswer(resultCaptor).when(spy).foo();

    Assertions.assertThat(resultCaptor.getResult()).isEqualTo("something");
    ```

#### Trackers

Tracker extension was added to simplify checking guice beans method arguments and result value,
because mockito spies are not very handy (see example above).

Tracker is declared with `@TrackBean`, but field type must be `Tracker<Service>` with target
service, declared as generic:

```java
public class Test {

    @TrackBean
    Tracker<MyService> tracker;

    @Inject
    MyService service;
    
    @Test
    public void test() {
        // call
        service.foo("something");

        MethodTrack track = serviceTracker.getLastTrack();
        Assertions.assertTrue(track.toString().contains("foo(\"something\")"));
        Assertions.assertEquals("something", track.getArguments()[0]);
        Assertions.assertNull(track.getResult());

    }
} 
```

!!! important "Limitation"
    Trackers implemented with guice AOP and so would **work only for beans, created by guice**. 

As method arguments (and return value) could contain mutable objects (which would lost 
the original state after several method calls), the tracker keeps both 
raw object version and string version: `getRawResult()` and `getResult()`,
`getRawArguments()` and `getArguments()`. 

Additionally, there are quoted versions: `getQuatedResult()` and `getQuatedArguments()`.
These methods are the same as string methods, but all strings are in quotes to clearly see
string bounds (quoted versions useful for console printing)

When too many method calls are tracked, keeping raw objects might lead to a waste of memeory,
in this case only string values could be preserved: `@TrackBean(keepRawObjects = false)`

!!! note
    By default, tracker data would reset after each test method. 
    Use `@TrackBean(autoRest = false)` to keep all data.

    Tracked data could be reset manually at any time with: `tracker.clear()`

##### Searching

All recorded method calls could be obtained with `List<MethodTrack> tracks = tracker.getTracks()`.
If there was just one call: `MethodTrack track = tracker.getLastTrack()`

In the case of many recorded executions, search could be used:

```java
// search by mehtod (any argument value)
tracks = tracker.findTracks(mock -> when(
               mock.foo(Mockito.anyInt()))
         );

// search methods with exact argument 
tracks = tracker.findTracks(mock -> when(
               mock.foo(Mockito.intThat(argument -> argument == 1)))
        );
```

This method uses Mockito stubbing abilities for search criteria declaration:
easy to use and type-safe search.

##### Performance metrics

There is a trace mode printing all method calls into console: `@TrackBean(trace = true)`:

```
\\\---[Tracker<Service>] 0.11 ms      <@71370fec> .foo(1) = "1 call"
```

Shows: instance hash, execution time, arguments and return value.

By default, tracker would only log executions for slow methods (more than 5 seconds):

```
WARN  [2025-04-01 09:22:28,965] ru.vyarus.dropwizard.guice.test.jupiter.ext.track.Tracker: 
\\\---[Tracker<Service>] 2.07 ms      <@53aa2fc9> .foo() = "foo"
```

Note that warning is printed with a logger, and not as direct console output like trace.
Slow methods configuration: `@TrackBean(slowMethods = 5, slowMethodsUnit = ChronoUnit.SECONDS)`
(`0` value to disable warnings).

When guice extension debug is enabled (`@TestGuiceyApp(..., debug = true)`), a performance report
for all registered tracker objects would be printed:

```
\\\------------------------------------------------------------/ test instance = 2bbb44da /
Trackers stats (sorted by median) for TrackerSimpleTest#testTracker():

	[service]                                [method]                                           [calls]    [fails]    [min]      [max]      [median]   [75%]      [95%]     
	Service                                  foo(int)                                           3          0          0.011 ms   0.161 ms   0.151 ms   0.161 ms   0.161 ms  
	Service                                  bar(int)                                           1          0          0.066 ms   0.066 ms   0.066 ms   0.066 ms   0.066 ms  
```

The report use dropwizard metrics to count percentiles. This is required to collect
more informative data when trying to use trackers for performance testing - 
due to jvm warm-up first executions would be much slower, but percentiles should
show more correct values (closer to hot execution). 

!!! tip
    For each tracker, an individual report could be activated with: `@TrackBeam(printSummary = true)`
    This report does not depend on the extension debug flag. 

The summary report also shows the number of service instances involved in stats (in the example 
trace was enabled for clarity):

```
\\\---[Tracker<Service>] 0.28 ms      <@6707a4bf> .foo(1) = "foo1"
\\\---[Tracker<Service>] 0.007 ms     <@79d3473e> .foo(2) = "foo2"

\\\------------------------------------------------------------/ test instance = 51f18e31 /
Tracker<Service> stats (sorted by median) for ReportForMultipleInstancesTest$Test1#testTracker():

	[service]                                [method]                                           [calls]    [fails]    [min]      [max]      [median]   [75%]      [95%]     
	Service                                  foo(int)                                           2 (2)      0          0.007 ms   0.281 ms   0.281 ms   0.281 ms   0.281 ms  
```

Note different instances in trace (`<@6707a4bf>`, `<@79d3473e>`) and instances count in calls column: `2 (2)`

Method stats (summary of all collected executions for method) could also be used for assertions:

```java
TrackerStats stats = tracker.getStats();
Assertions.assertEquals(1, stats.getMethods().size());

MethodSummary summary = stats.getMethods().get(0);
Assertions.assertEquals("foo", summary.getMethod().getName());
Assertions.assertEquals(Service.class, summary.getService());
Assertions.assertEquals(1, summary.getTracks());
Assertions.assertEquals(0, summary.getErrors());
Assertions.assertEquals(1, summary.getMetrics().getValues().length);
Assertions.assertTrue(summary.getMetrics().getMin() < 1000);
```

#### REST stubs

Guicey provides lightweight REST testing (the same as [dropwizard resource testing support](https://www.dropwizard.io/en/stable/manual/testing.html#testing-resources),
but with guicey-specific features).
Such tests would not start web container: all rest calls are simulated (but still, it tests every part of resource execution).

Lightweight REST could be declared with `@StubRest` annotation under `@TestGuiceyApp` extension:

```java
@TestGuiceyApp(...)
public class Test {
    
    @StubRest
    RestClient rest;
    
    @Test
    public void test() {
        String res = rest.get("/foo", String.class);
        Assertions.assertEquals("something", res);

        WebApplicationException ex = Assertions.assertThrows(WebApplicationException.class,
                () -> rest.get("/error", String.class));
        Assertions.assertEquals("error message", ex.getResponse().readEntity(String.class));
    }
}
```

!!! note
    Extension naming is not quite correct: it is not a stub, but real application resources are used.
    The word "stub" used to highlight the fact of incomplete startup: only rest without web.

By default, all declared resources would be started with all existing jersey extensions
(filters, exception mappers, etc.). **Servlets and http filters are not started** 
(guicey disables all web extensions to avoid their (confusing) appearance in console)

##### Selecting resources

Real tests usually require just one resource (to be tested):

```java
@StubRest(MyResource.class)
RestClient rest;
```

This way only one resource would be started (and all resources directly registered in 
application, not as guicey extension). All jersey extensions will remain.

Or a couple of resources:

```java
@StubRest({MyResource.class, MyResource2.class})
RestClient rest;
```

Or you may disable some resources:

```java
@StubRest(disableResources = {MyResource2.class, MyResource3.class})
RestClient rest;
```

##### Disabling jersey extensions

Often jersey extensions, required for the final application, make complications for testing.

For example, exception mapper: dropwizard register default exception mapper which
returns only the error message, instead of actual exception (and so sometimes we can't check the real cause).

`disableDropwizardExceptionMappers = true` disables extensions, registered by dropwizard.

When default exception mapper enabled, resource throwing runtime error would return:

```java
@Path("/some/")
@Produces("application/json")
public class ErrorResource {

    @GET
    @Path("/error")
    public String get() {
        throw new IllegalStateException("error");
    }
}    
```

```java
@TestGuiceyApp
public class Test {

    @StubRest
    RestClient rest;

    public void test() {
        WebApplicationException ex = Assertions.assertThrows(WebApplicationException.class,
                () -> rest.get("/some/error", String.class));
        
        // exception hidden, only generic error code
        Assertions.assertTrue(ex.getResponse().readEntity(String.class)
                .startsWith("{\"code\":500,\"message\":\"There was an error processing your request. It has been logged"));
    }
}
```

```java
public class Test {

    @StubRest(disableDropwizardExceptionMappers = true)
    RestClient rest;
    
    public void test() {
        ProcessingException ex = Assertions.assertThrows(ProcessingException.class,
                () -> rest.get("/error", String.class));
        // exception available
        Assertions.assertTrue(ex.getCause() instanceof IllegalStateException);
    }
}
```
                                                                      
It might be useful to disable application extensions also with `disableAllJerseyExtensions`:

```java
@StubRest(disableDropwizardExceptionMappers = true, 
          disableAllJerseyExtensions = true)
RestClient rest;
```

This way raw resource would be called without any additional logic.

!!! note
    Only extensions, managed by guicey could be disabled: extensions directly registered
    in dropwizard would remain.

Also, you can select exact extensions to use (e.g., to test it):

```java
@StubRest(jerseyExtensions = CustomExceptionMapper.class)
RestClient rest;
```

Or disable only some extensions (for example, disabling extension implementing security):

```java
@StubRest(disableJerseyExtensions = CustomSecurityFilter.class)
RestClient rest;
```

##### Debug

Use **debug** output to see what extensions were actually included and what disabled:

```java
@TestGuiceyApp(.., debug = true)
public class Test {
    @StubRest(disableDropwizardExceptionMappers = true,
            disableResources = Resource2.class,
            disableJerseyExtensions = RestFilter2.class)
    RestClient rest;
}
```

```
REST stub (@StubRest) started on DebugReportTest$Test1:

	Jersey test container factory: org.glassfish.jersey.test.inmemory.InMemoryTestContainerFactory
	Dropwizard exception mappers: DISABLED

	2 resources (disabled 1):
		ErrorResource                (r.v.d.g.t.j.s.r.support)  
		Resource1                    (r.v.d.g.t.j.s.r.support)  

	2 jersey extensions (disabled 1):
		RestExceptionMapper          (r.v.d.g.t.j.s.r.support)  
		RestFilter1                  (r.v.d.g.t.j.s.r.support)  

	Use .printJerseyConfig() report to see ALL registered jersey extensions (including dropwizard)
```

##### Requests logging

By default, rest client would not log requests and responses, but logging could be enabled
with `logRequests = true`:

```java
@TestGuiceyApp(...)
public class Test {
    
    @StubRest(logRequests = true)
    RestClient rest;
    
    @Test
    public void test() {
        String res = rest.get("/foo", String.class);
        Assertions.assertEquals("something", res);
    }
}
```

```
[Client action]---------------------------------------------{
1 * Sending client request on thread main
1 > GET http://localhost:0/foo

}----------------------------------------------------------


[Client action]---------------------------------------------{
1 * Client response received on thread main
1 < 200
1 < Content-Length: 3
1 < Content-Type: application/json
something

}----------------------------------------------------------
```

##### Container

By default, [InMemoryTestContainerFactory](https://eclipse-ee4j.github.io/jersey.github.io/documentation/latest/test-framework.html#d0e18552)
used.

    In-Memory container is not a real container. It starts Jersey application and 
    directly calls internal APIs to handle request created by client provided by 
    test framework. There is no network communication involved. This containers 
    does not support servlet and other container dependent features, but it is a 
    perfect choice for simple unit tests.

If it is not enough (in-memory container does not support all functions), then
use `GrizzlyTestContainerFactory`

    The GrizzlyTestContainerFactory creates a container that can run as a light-weight, 
    plain HTTP container. Almost all Jersey tests are using Grizzly HTTP test container 
    factory.

To activate grizzly container add dependency (version managed by dropwizard BOM):

```groovy
testImplementation 'org.glassfish.jersey.test-framework.providers:jersey-test-framework-provider-grizzly2'
```

By default, `@StubRest` would use grizzly, if it's available on classpath or in-memory.
If you need to force any container type use:

```java
// use in-memory container, even if grizly available in classpath
// (use to force more lightweight container, even if some tests require grizzly)
@StubRest(container = TestContainerPolicy.IN_MEMORY)
```

```java
// throw error if grizzly container not available in classpath
// (use to avoid accidental in-memory use)
@StubRest(container = TestContainerPolicy.GRIZZLY)
```

##### Rest client

`RestClient` is almost the same as `ClientSupport`, available for guicey extensions.
It is just limited only for rest (and so simpler to use).

!!! note
    Just in case: `ClientSupport` would not work with `@StubRest`    

Client provides base methods with response mapping:

```java
@StubRest
RestClient rest;
```

* `rest.get(path, Class)`
* `rest.post(path, Object/Entity, Class)`
* `rest.put(path, Object/Entity, Class)`
* `rest.delete(path, Class)`

To not overload default methods with parameters, additional data could be set with defaults:

* `rest.defaultHeader(String, String)`
* `rest.defaultQueryParam(String, String)`
* `rest.defaultAccept(String...)`
* `rest.defaultOk(Integer...)`

`defaultOk` used for void responses (response class == null) to check correct response 
status (default 200 (OK) and 204 (NO_CONTENT)).

So if we need to perform a post request with query param and custom header:

```java
rest.defaultHeader("Secret", "unreadable")
    .defaultQueryParam("foo", "bar");
OtherModel res = rest.post("/somehere", new SomeModel(), OtherModel.class);
```

!!! note
    Multipart support is enabled automatically when dropwizard-forms available in classpath
    
    ```java
    FormDataMultiPart multiPart = new FormDataMultiPart();
    multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

    FileDataBodyPart fileDataBodyPart = new FileDataBodyPart("file",
            file.toFile(),
            MediaType.APPLICATION_OCTET_STREAM_TYPE);
    multiPart.bodyPart(fileDataBodyPart);

    rest.post(path, Entity.entity(multiPart, multiPart.getMediaType()), Something.class);
    ```

To clear defaults: 

```java
rest.reset() 
```

Might be a part of call chain:

```java
rest.reset().post(...) 
```

When test needs to verify cookies, response headers, etc. use `.request(path)`:

```java
Response response = rest.request(path).get() // .post(), .put(), .delete();
```

All defaults are also applied in this case.

To avoid applying configured defaults, raw `rest.target(path)...` could be used.

#### Testing logs

!!! important
    Logs testing works only with logback!

`@RecordLogs` extension could record log messages for one or multiple classes:

```java
@TestGucieyApp(...)
public class Test {
    
    @RecordLogs(value = Service.class, level = Level.DEBUG)
    RecordedLogs logs;
    
    @Inject
    Service service;
    
    @Test
    public void test() {
        // here some actions with service, involving logging
        service.doSomething();

        Assertions.assertEquals(2, logs.count());
        Assertions.assertTrue(logs.has(Level.DEBUG));
        Assertions.assertEquals(Arrays.asList("message 1", "message 2"), 
                logs.messages());
    }
}
```

Logs could be collected for any custom logger name or entire package:

```java
@RecordLogs(loggers = "ru.vyarus.dropwizard.guice.test", level = Level.TRACE)
RecordedLogs logs;
```

!!! important
    To collect logs, logger level must be set to the required level and so these logs would 
    also appear in console output. 
    This allows using log recorder as a simple way to enable required logs for tests.

Dropwizard resets logging two times during startup, but the extension also configures required
loggers multiple times. In most cases, all required logged messages should be intercepted.

To avoid tons of selection methods with different parameters, all selection methods
return sub-selector object for further selections. 
For example, to select messages by level and logger:

```java
logs.logger(SomeClass.class).level(Level.DEBUG).messages().
```

Also, original event objects are available: `.events()`.

## Internal 

### BeforeInit event

Added `BeforeInitEvent` - ("meta" event) the first point where `Bootstrap` reference is available
(`GuiceBundle` initialization started), but guicey actually did not start any actions, except
hooks processing.

Example usage:

```java
GuiceBundle.builder()
    .listen(new GuiceyLifecycleAdapter(){
        @Override
        protected void beforeInit(final BeforeInitEvent event) { ... }
    })
```

Event used by the new startup time report (to modify `Bootstrap` object for execution 
time tracking).

### Web installers marker

Added a new marker interface `WebInstaller`. It must be applied for all
web (jetty) and jersey related installers.

Marker used to mark extensions as web extensions and be able to disable them
all at once (for example):

```java
@EnableHook
static GuiceyConfigurationHook hook = builder ->
        builder.disable(webExtension());
```

This is used in the new rest stubs to disable web extensions (not started with the jersey test 
support to avoid confusing console output).

## Migration guide

* If guicey shared state was used, then you'll have to update places accessing stored objects:
  before it was recommended to use bundle name as a key, now stored object class must be used
  (initial approach was not very easy to use (even confusing) now access is type-safe)
* Guicey now analyzes bindings exposed from private guice modules to detect extensions.
  Also, disabling extension, declared in private module would lead to private binding remove.  
  If any problems appear, old behavior could be restored with:
  `.option(GuiceyOptions.AnalyzePrivateGuiceModules, false)`
* If junit 5 used for tests, pay attention to new features:
  - Configuration modifiers (could be more useful than the configuration overrides mechanism)
  - `GuiceyEnvironmentSetup` objects now have direct access to junit context and test lifecycle events
    and so could completely replace some junit extensions (where guicey integration is required)
  - New field-based extensions:
        * `@StubBean` - replacing guice beans with custom stubs (without additional modules declaration)
        * `@MockBean` - replacing guice beans with Mockito mocks
        * `@SpyBean` - replacing guice beans with Mockito spies
        * `@TrackBean` - simpler alternative to Mockito spies to track guice beans methods calls
        * `@StubRest` - lightweight rest services testing (without starting web)
        * `@RecordLogs` - test logs 
