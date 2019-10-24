# Diagnostic tools

Guicey provide many bundled console reports to help with problems diagnostic (or to simply clarify how application works)
during development. All reports may be enabled on main guice bundle:

`#!java .printDiagnosticInfo()`  
:   Detailed [guicey configuration](configuration-report.md) information

`#!java .printAvailableInstallers()`
:   Prints [available installers](installers-report.md) (helps understanding supported extensions)

`#!java .printCustomConfigurationBindings()`  
`#!java .printConfigurationBindings()`     
:   Show [yaml config introspection result](yaml-values-report.md) (shows available yaml value bindings)
  
`#!java .printGuiceBindings()`  
`#!java .printAllGuiceBindings()`
:   [Guice bindings](guice-report.md) from registered modules    

`#!java .printGuiceAopMap()`  
`#!java .printGuiceAopMap(GuiceAopConfig config)`
:   [AOP appliance](aop-report.md) map

`#!java .printWebMappings()`
:   Prints all registered [resvlets and filters](web-report.md) (including guice `ServletModule` declarations)
  
`#!java .printLifecyclePhases()`  
`#!java .printLifecyclePhasesDetailed()`  
:   [Guicey lifecycle stages](lifecycle-report.md) (separates logs to clearly see what messages relates to what phase)

`#!java .strictScopeControl()`
:   In case of doubts about extension owner (guice or HK2) and suspicious for duplicate instantiation, 
    you can enable [strict control](../hk2.md#hk2-scope-debug) which will throw exception in case of wrong owner.   

## Diagnostic hook

It is obviously impossible to enable diagnostic reports without application re-compilation. 
But, sometimes, it is required to validate installed application. To workaround this situation,
guicey provides special diagnostic hook, which can be enabled with a system property:

```
-Dguicey.hooks=diagnostic
```

Hook activates the most commonly used reports:

```java
public class DiagnosticHook implements GuiceyConfigurationHook {
    public void configure(final GuiceBundle.Builder builder) {
        builder.printDiagnosticInfo()
                .printLifecyclePhasesDetailed()
                .printCustomConfigurationBindings()
                .printGuiceBindings();
    }
}
``` 

!!! tip
    If provided hook doesn't cover all required reports, you can always make your own hook
    and [register it's shortcut](../configuration.md#hooks-related) for simplified usage 

## Reports implementation

Report is implemented as guicey [event listener](../events.md). All sub-reports provide additional configuration
options, so if default configuration (from shortcut methods above) does not fit your needs
you can register listener directly with required configuration.

For example, available installers report is re-configured configuration report:

```java
public Builder<T> printAvailableInstallers() {
    return listen(ConfigurationDiagnostic.builder("Available installers report")
            .printConfiguration(new DiagnosticConfig()
                    .printInstallers()
                    .printNotUsedInstallers()
                    .printInstallerInterfaceMarkers())
            .printContextTree(new ContextTreeConfig()
                    .hideCommands()
                    .hideDuplicateRegistrations()
                    .hideEmptyBundles()
                    .hideExtensions()
                    .hideModules())
            .build());
}
```

Report rendering logic may also be used directly as all reports (except lifecycle) provide separate renderer object
implementing `ReportRenderer`. Renderers not bound to guice context and assume direct instantiation. 

For examples of direct renderers usage see [events](../events.md) implementation:

* `RunPhaseEvent.renderConfigurationBindings()`
* `InjectorPhaseEvent.ReportRenderer` 

!!! note
    These shortcut methods allow easy render of report into string 
    using received event object (in listener). 
 