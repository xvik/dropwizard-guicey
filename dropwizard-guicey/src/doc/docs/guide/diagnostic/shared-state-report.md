# Shared state usage

Guicey [shared state](../shared.md) is a bundle communication mechanism and safe "static" access
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