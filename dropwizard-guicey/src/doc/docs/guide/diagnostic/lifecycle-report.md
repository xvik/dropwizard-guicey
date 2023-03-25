# Lifecycle report

In contrast to other reports, this report prints many small messages: 1 message on each meaningful 
guicey lifecycle points. This lets you:

* See how configuration goes (and so on each stage problem appear)
* Separate your logs (making entire startup operations order more obvious)
* Indicate time since application startup making obvious how much time application
spent on each phase.

```java
GuiceBundle.builder()
    ...
    .printLifecyclePhases()
     // or printLifecyclePhasesDetailes() to see all confgiruation objects used on logged stage 
    .build()
```

Example output:    

```
                                                                         ─────────────────
__[ 00:00:00.013 ]____________________________________________________/  1 hooks processed  \____



                                                                         ────────────────────────────────
__[ 00:00:00.121 ]____________________________________________________/  Initialized 3 (-1) GuiceyBundles  \____



                                                                         ────────────────────
__[ 00:00:00.136 ]____________________________________________________/  2 commands installed  \____



                                                                         ──────────────────────────────
__[ 00:00:00.151 ]____________________________________________________/  11 (-1) installers initialized  \____



                                                                         ────────────────────────────────
__[ 00:00:00.158 ]____________________________________________________/  14 classpath extensions detected  \____

INFO  [2019-10-13 08:33:33,925] io.dropwizard.server.DefaultServerFactory: Registering jersey handler with root path prefix: /
INFO  [2019-10-13 08:33:33,926] io.dropwizard.server.DefaultServerFactory: Registering admin handler with root path prefix: /


                                                                         ───────────────────────
__[ 00:00:00.869 ]____________________________________________________/  Started 3 GuiceyBundles  \____
...
```   

!!! important
    Lifecycle logs are printed to system out instead of logger because logger is not yet initialized on first events. 
    Anyway, these logs intended to be used for problems resolution and so console only output should not be a problem.

!!! note
    `(-1)` in some events in report means disabled items and actual displayed count did not count disabled items.
    You can see all disabled items on [detailed output](#detailed-output).

## Detailed output

Detailed report (`#!java .printLifecyclePhasesDetailed()`) shows all configuration objects used at current stage. 

Example detailed output:

```
                                                                         ─────────────────
__[ 00:00:00.013 ]____________________________________________________/  1 hooks processed  \____

	hooks = 
		GuiceyTestHook               (r.v.d.g.AbstractTest)     



                                                                         ────────────────────────────────
__[ 00:00:00.118 ]____________________________________________________/  Initialized 3 (-1) GuiceyBundles  \____

	bundles = 
		GuiceRestrictedConfigBundle  (r.v.d.g.support.util)     
		CoreInstallersBundle         (r.v.d.g.m.installer)      
		WebInstallersBundle          (r.v.d.g.m.installer)      

	disabled = 
		HK2DebugBundle               (r.v.d.g.m.j.debug)        

	ignored duplicate instances = 
		HK2DebugBundle               (r.v.d.g.m.j.debug)        



                                                                         ────────────────────
__[ 00:00:00.131 ]____________________________________________________/  2 commands installed  \____

	commands = 
		DummyCommand                 (r.v.d.g.s.feature)        
		NonInjactableCommand         (r.v.d.g.s.feature)        

...    
```  

## Timer

Each log contains time marker:

```
__[ 00:00:00.131 ]___
```                           

Timer starts on `GuiceBundle` creation (it is physically impossible to start it earlier).

Note that timer also measure shutdown time (starting from initial shutdown start event):

```
                                                                         ─────────────────
__[ 00:00:00.000 ]____________________________________________________/  Stopping Jetty...  \____

INFO  [2019-10-13 08:36:02,882] org.eclipse.jetty.server.AbstractConnector: Stopped application@36cc9385{HTTP/1.1,[http/1.1]}{0.0.0.0:8080}
INFO  [2019-10-13 08:36:02,883] org.eclipse.jetty.server.AbstractConnector: Stopped admin@cf518cf{HTTP/1.1,[http/1.1]}{0.0.0.0:8081}
INFO  [2019-10-13 08:36:02,886] org.eclipse.jetty.server.handler.ContextHandler: Stopped i.d.j.MutableServletContextHandler@5a484ce1{Admin context,/,null,UNAVAILABLE}


                                                                         ────────────────────
__[ 00:00:00.000 ]____________________________________________________/  Jersey app destroyed  \____

INFO  [2019-10-13 08:36:02,892] org.eclipse.jetty.server.handler.ContextHandler: Stopped i.d.j.MutableServletContextHandler@3cc30dee{Application context,/,null,UNAVAILABLE}


                                                                         ─────────────
__[ 00:00:00.000 ]____________________________________________________/  Jetty stopped  \____
```

This may help you to spot slow shutdown logic.