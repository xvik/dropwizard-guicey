# Guice provision time

The report intended to show the time of guice beans provision (instance construction,
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

```
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

The report shows only provisions performed on application startup, but it could be used in
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