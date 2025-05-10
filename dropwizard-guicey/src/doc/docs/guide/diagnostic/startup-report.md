# Startup times

The report intended to show the entire application startup time information to simplify
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

## Before time

The report can't know what was happening before application initialization (jvm startup time),
but this is usually a meaningful time (while real person waits for application startup).

So all time before application is indicated with (value obtained from MX bean): 

`JVM time before                    : 1055 ms`