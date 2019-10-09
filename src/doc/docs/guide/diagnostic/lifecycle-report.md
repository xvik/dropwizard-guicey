# Lifecycle event

Use `bundle.printLifecyclePhases()` to see lifecycle events in logs.
Could be very helpful during problems investigation. Also, it shows startup timer to easily see where most startup time is spent.

Example output:

```

                                                                         ─────────────────────────
__[ 00:00:00.008 ]____________________________________________________/  1 hooks processed  \____



                                                                         ────────────────────
__[ 00:00:00.104 ]____________________________________________________/  2 commands installed  \____

INFO  [2018-06-15 04:09:56,978] io.dropwizard.server.DefaultServerFactory: Registering jersey handler with root path prefix: /
INFO  [2018-06-15 04:09:56,981] io.dropwizard.server.DefaultServerFactory: Registering admin handler with root path prefix: /


                                                                         ────────────────────────────────────
__[ 00:00:00.881 ]____________________________________________________/  Configured from 3 (-1) GuiceyBundles  \____



                                                                         ──────────────────────────────────────
__[ 00:00:00.886 ]____________________________________________________/  Staring guice with 3/0 (-1) modules...  \____



                                                                         ─────────────────────────────
__[ 00:00:00.980 ]____________________________________________________/  8 (-1) installers initialized  \____



                                                                         ────────────────────────
__[ 00:00:01.008 ]____________________________________________________/  13 (-1) extensions found  \____

...
```

Note that `(-1)` in some events means disabled items and actual displayed count did not count disabled items.
Use detailed output (`bundle.printLifecyclePhasesDetailed()`) to see more details. 

Example detailed output (for one event):

```
                                                                         ────────────────────────────────────
__[ 00:00:00.893 ]____________________________________________________/  Configured from 3 (-1) GuiceyBundles  \____

	bundles = 
		DiagnosticBundle             (r.v.d.g.m.c.debug)        
		GuiceRestrictedConfigBundle  (r.v.d.g.support.util)     
		CoreInstallersBundle         (r.v.d.g.m.installer)      

	disabled = 
		HK2DebugBundle               (r.v.d.g.m.j.debug)      
``` 

!!! important
    Lifecycle logs are printed to system out instead of logger because logger is not yet initialized on first events. 
    Anyway, these logs intended to be used for problems resolution and so console only output should not be a problem. 
