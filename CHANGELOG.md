* Update to dropwizard 5 (requires java 17)
* [admin-rest]
  - Add identifyAdminContextInRequestLogs bundle option to highlight admin requests in logs

### 7.1.4 (2024-09-14)
* Update to dropwizard 4.0.8

### 7.1.3 (2024-03-31)
* Update to dropwizard 4.0.7
* Fix guicey ApplicationShutdownEvent typo (#387)
          
NOTE: If your code uses this event directly (name with typo: ApplicationShotdownEvent), then it would 
be a breaking change and event name must be corrected manually in your code (to ApplicationShutdownEvent).
Sorry, can't do it in a backwards-compatible way, but I assume rare usage so should not affect many.

### 7.1.2 (2024-02-17)
* Update to dropwizard 4.0.6

### 7.1.1 (2024-01-08)
* Update to dropwizard 4.0.5

### 7.1.0 (2023-11-28)
* Update to dropwizard 4.0.4
* Add qualifier annotations support for configuration properties binding: 
    any configuration property (any level), annotated with qualifier annotation, would be 
    directly bound with that qualifier. Core dropwizard objects could be qualified on overridden getter
* Test improvements:
  - Junit 5 extensions could inject DropwizardTestSupport object itself as test method parameter
  - ClientSupport: 
      * inner jersey client creation is customizable now with TestClientFactory implementation
        (new attribute "clientFactory" in @TestGuiceyApp and @TestDropwizardApp)
      * default factory would automatically configure:
         - multipart feature if available in classpath (dropwizard-forms)
         - direct console logging (to see requests and responses directly in console)
      * New methods:
         - basePathRoot - root url (only with port)
         - get(), post(), delete(), put() - simple shortcut methods to perform basic operations relative to server root
  - Context support object (DropwizardTestSupport) and client (ClientSupport) instances are accessible now statically
      for both manual run (TestSupport) and junit extensions: TestSupport.getContext() and TestSupport.getContextClient()
  - New generic builder for flexible DropwizardTestSupport object creation and run (when junit extension can't be used):
      TestSupport.builder() (with lifecycle listeners support)
  - TestSupport methods changes: 
      * Creation and run methods updated with config override (strings) support 
      * Add creation and run methods application class only (and optional overrides).
      * Run methods without callback now return RunResult containing all objects, required for validation (for example, to examine config)
      * Add captureOutput method to record console output for assertions
  - Commands test support: 
      * TestSupport.buildCommandRunner() - builds runner for command execution
          with the same builder options as in generic builder (TestSupport.builder(); including same configuration) 
          and user input support.
      * Could be used to test application startup fail (without using system mocks)

### 7.0.2 (2023-10-06)
* Update to dropwizard 4.0.2

### 7.0.1 (2023-07-05)
* Update to dropwizard 4.0.1
* [jdbi]
  - Fix jdbi 3.39 compatibility
  - Avoid redundant transaction isolation level checks (extra queries) (#318)
* [gsp]
  - Fix redirection to error page after direct template rendering fails

### 7.0.0 (2023-05-14)
* Update to dropwizard 4
  - (breaking) Use jakarta namespace instead of javax (servlet, validation)
* Update to guice 7 (jakarta.inject namespace)    

### 6.1.0 (2023-05-14)
* Update to guice 6.0

### 6.0.0 (2023-04-02)
* Update to dropwizard 3
  - (breaking) Drop java 8 support
* Merged with guicey-ext modules repository: 
  - Ext modules version would be the same as guicey
  - dropwizard-guicey POM would not be a BOM anymore (everything moved to guicey-bom)
  - Exclusions not applied in BOM anymore, instead they applied directly in POM

### 5.7.1 (2023-03-09)
* Update to dropwizard 2.1.5
* Revert changing reports log level: now INFO used instead of WARN (#276) 

### 5.7.0 (2022-12-29)
* Update to dropwizard 2.1.4
* Fix NoClassDefFoundError(AbstractCollectionJaxbProvider) appeared for some jersey provider registrations (#240)
* Jersey extensions might omit `@Provider` on known extension types (ExceptionMapper, MessageBodyReader, etc.). 
  Unifies usage with pure dropwizard (no additional `@Provider` annotation required). (#265)
    - New option InstallerOptions.JerseyExtensionsRecognizedByType could disable new behaviour
* Support ModelProcessor jersey extension installation (#186)
* Add extensions help: .printExtensionsHelp() showing extension signs recognized by installers (in recognition order)
  - Custom installers could participate in report by overriding FeatureInstaller.getRecognizableSigns()
    (default interface method).
* Change reports log level from INFO to WARN to comply with default dropwizard level
* Support application reuse between tests (#269)
  - new reuseApplication parameter in extensions enables reuse
  - reusable application must be declared in base test class: all tests derived
    from this base class would use the same application instance
* Add SBOM (json and xml with cyclonedx classifier)
* Add .enableAutoConfig() no-args shortcut for enabling classpath scan in application package

### 5.6.1 (2022-07-02)
* Update dropwizard to 2.1.1 (fixes java 8 issue by allowing afterburner usage)
* Fix classpath scan recognition of inner static classes inside jars (#231)
* Junit 5 extensions:
  - Fix parallel test methods support (configuration overrides were applied incorrectly) 
  - Add "debug" option: when enabled, prints registered setup objects, hooks and 
    applied configuration overrides
      * Setup objects and hooks not printed by default as before, only when debug enabled
      * Debug could be also enabled with system property -Dguicey.extensions.debug=true
        or with alias TestSupport.debugExtensions()

### 5.6.0 (2022-06-07)
* Update dropwizard to 2.1.0
* Test support objects changes:
    - Add new interface TestEnvironmentSetup to simplify test environment setup
        * In contrast to guicey hooks, setup objects used only in tests to replace the need of writing 
          additional junit extensions (for example, to setup test db). It provides a simple way to
          override application configuration (e.g. to specify credentials to just started db)
        * Registration is the same as with hooks: annotation or inside extension builder and with 
          field using new annotation @EnableSetup
    - Hooks and setup objects configured in test are logged now in execution order and
      with registration source hint
    - @EnableHook fields might be declared with custom classes (not only raw hook interface)
* Junit 5 extensions field registration (@RegisterExtension) changes 
  - Application might be started per-test-method now (when extension registered in non-static field)
      * In this case support objects might also be registered in non-static fields
  - Add configOverrideByExtension method to read configuration override value
    registered by 3rd party junit 5 extension (from junit extension store).
  - hooks(Class) method accepts multiple classes
  - configOverrides(String...) now aggregates multiple calls

Known issue:
* Dropwizard replaced jackson afterburner with blackbird. On java 8 this
  leads to a warning on startup that looks like exception. Everything works,
  just a very confusing stacktrace on startup (https://github.com/xvik/dropwizard-guicey/discussions/226) 

### 5.5.0 (2022-03-30)
* Test framework-agnostic utilities:
  - Add GuiceyTestSupport to simplify guice-only manual application runs 
    (by analogy to DropwizardTestSupport class)
  - Add TestSupport class as a root for test framework-agnostic utilities.
    Suitable for application startup errors testing and integration within not supported test runner.
* Add Spock 2 support: there is no custom extensions, instead existing junit 5 extensions would be used
    through a special library spock-junit5 (developed specifically for this integration)
* Change "hooks in base test class" behaviour: hooks from static fields from base classes applied before hooks in test itself.
  Such behaviour is more natural - "base classes declarations go first"
  (before all field hooks were applied after annotation hooks)
* Extract Spock 1 and Junit 4 extensions from core into ext modules: 
  - packages remain the same, so there should be no issues with it (just add new dependency)
  - removed deprecation markers from Junit 4 rules (entire module assumed to be deprecated; fewer warnings on build)
* BOM changes:
  - spock version removed in order to avoid problems downgrading spock version for spock1 module
  - system-rules removed because it targets junit4 (ext module provides it)
  - groovy libraries removed (newer groovy 2.x was required for spock1 to run on java 11)
  - add spock-junit5 version 
  
### 5.4.2 (2022-01-26)
* Update dropwizard to 2.0.28
* Update guice to 5.1.0 (java 17 support)

### 5.4.1 (2021-12-19)
* Fix inner guice class usage in always executable code (#187, OSGi issue)
* Update dropwizard to 2.0.27 (many dependency updates in the latest versions fixing java 17 support)

### 5.4.0 (2021-10-21)
* Use direct dependency versions in pom to simplify resolution (dependencyManagement section remains but for usage as BOM only)
* Fix lambda modules support (modules declared with lambda expression) (#160)
* Exclude "sun.*" objects from configuration analysis (#170, #180)
* Fix junit5 extensions support for @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  (class instance injections now processed in beforeEach hook instead of instancePostProcessor)
* Add error message when junit5 extensions incorrectly registered with non-static fields (to avoid confusion)
* SharedConfigurationState:
  - Add ability for direct static lookup during startup (from main thread):
     SharedConfigurationState.getStartupInstance() 
     (option required for places where neither application not environment object accessible
     (e.g. binding installer, bundle lookup, etc.); and could be used for common objects resolution where otherwise 
     they are not directly accessible)
  - Add shortcut methods on state instance producing providers (lazy) for common objects:
     getBootstrap, getApplication, getEnvironment, getConfiguration, getConfigurationTree
     (example usage during startup: SharedConfigurationState.getStartupInstance().getApplication() returns Provider<Application>)
* Unify shared state access methods in GuiceyBootstrap, GuiceyEnvironment and DropwizardAwareModule
  (removes implicit limitation that shared state must be initialized only in init phase)

Lambda modules reporting:
- Root lambda module class will be shown in the diagnostic report (in a list of root modules)
- Guice bindings report:
  * Will show all root lambda modules bindings below to com.google.inject.Module
    (code links at the end of each binding would lead to correct declaration line)
  * Bindings of lambda modules installed by root (or deeper modules) would be shown
      directly under root module, as if it was declared directly in that module (logically it's correct)

IMPORTANT: since dropwizard 2.0.22 [jdk8 compatibility is broken for jdbi3](https://github.com/dropwizard/dropwizard/releases/tag/v2.0.22). 
To bring back jdk8 compatibility caffeine version must be downgraded.
There is now a special package guicey-jdbi3-jdk8 for easy compatibility fixing.

### 5.3.0 (2021-03-06)
* Update to guice [5.0.1](https://github.com/google/guice/wiki/Guice501) 
  (java15 support, removes cglib, fixes "illegal reflective access" warnings, update Guava to LATEST 30.1-jre)
* Update to dropwizard 2.0.20
* Unify GuiceyAppRule (junit4) behaviour with DropwizardAppRule: config overrides should initialize just 
  before test and not in the constructor. The issue was causing early evaluation of lazy (deferred) overrides (#151)
* Add custom ConfigOverride objects support for junit 5 extensions (registered with @RegisterExtension)    
  
NOTE: it would not be possible to downgrade guice dependency (to 4.x) due to renamed (internal) guice class, used by guicey

### 5.2.0 (2020-11-29)
* Update to dropwizard 2.0.16
* Remove direct usages of logback-classic classes to unlock logger switching (#127)
* Fix stackoverflow on config introspection caused by EnumMap fields (#87) 
* Prioritize registered jersey provider extensions and add support for @Priority annotation (#97)
  Unifies raw dropwizard and guicey behaviour. Possibly breaking, see note below.
* Add lifecycle event: ApplicationStoppedEvent (triggered on jersey lifecycle stop)   

NOTE: 
In raw dropwizard registered jersey extension (with environment.jersey().register(MyExceptionMapper.class))
is implicitly qualified as @Custom and always used in priority comparing to default dropwizard providers.

Before, guicey was registering provider extensions without this qualifier and so the default 
dropwizard providers were used in priority (as registered earlier).
For example, it was impossible to register ExceptionMapper<Throwable> because dropwizard already registered one.
Now your custom mapper will be used in priority and so it is possible to override default `ExceptionMapper<Throwable>`
(for example).

This COULD (unlikely, but still) change application behaviour: your custom provider could be called in more cases.
But, as this behaviour is the default for raw dropwizard, the change considered as a bugfix.
In case of problems, you could revert to legacy guicey behaviour with: 
    .option(InstallerOptions.PrioritizeJerseyExtensions, false)     

### 5.1.0 (2020-06-02)
* Update guice to 4.2.3 ([java 14 support](https://github.com/google/guice/wiki/Guice423#changes-since-guice-422))
* Update to dropwizard 2.0.10
* Add junit 5 extensions (#74). Works much like existing spock extensions:
    - @TestGuiceyApp for replacement of GuiceyAppRule
    - @TestDropwizardApp for using instead of DropwizardAppRule (or current dropwizard extension)
* Spock extensions updates:
    - Internally, use DropwizardTestSupport instead of deprecated junit 4 rules
    - New features (port features from junit 5 extensions):
        * @UseDropwizardApp got new configurations: randomPorts and restMapping 
        * @UseGuiceyHooks deprecated: instead additional hooks may be declared in static test field
        * ClientSupport test field will be injected with client support object instance
* Junit 4 rules deprecated GuiceyAppRule, StartupErrorRules             
* Fix parallel tests support: guice logs interception wasn't thread safe (#103)     
* Fix invalid Automatic-Module-Name to 'ru.vyarus.dropwizard.guicey' (#106) 

### 5.0.1 (2020-03-13)
* Update to dropwizard 2.0.2 (address [CVE-2020-5245](https://github.com/advisories/GHSA-3mcp-9wr4-cjqf))
* Fix yaml bindings report rendering with values containing string format parameters like %s (#77) 

### 5.0.0 (2019-12-15)
* Update to dropwizard 2.0.0
    - (breaking in jersey 2.26)
        * Jersey 2.26 introduces an abstraction for injection layer in order to get rid of hk2 direct usage.
          This allows complete hk2 avoidance in the future. Right now it means that all direct hk2 classes must be replaced
          by jersey abstractions (but still hk2 is the only production ready integration)
            - Jersey `InjectionManager` now bound to guice context instead of hk2 `ServiceLocator` 
                (locator still can be retrieved from manager)
            - Rename HK2 mentions into jersey (because now jersey is not tied to hk2)
                 * `@HK2Managed` renamed to `@JerseyManaged`   
            - JerseyProviderInstaller (installs classes annotated with `@Provider`) changes:     
                * `ValueParamProvider` detected instead of `ValueFactoryProvider`  
                * `Supplier` detected instead `Factory` (Factory implementations are not recognized anymore!)
                * `org.glassfish.jersey.internal.inject.InjectionResolver` detected instead of `org.glassfish.hk2.api.InjectionResolver`
            - Jersey installers use `org.glassfish.jersey.internal.inject.AbstractBinder`
              instead of hk specific `org.glassfish.hk2.utilities.binding.AbstractBinder`
            - Mark all hk2-related methods and options as deprecated (to be removed in the next version)       
        * Jersey 2.26 implements jax-rs 2.1 which forced it to change some of it's apis.
            - `org.glassfish.jersey.server.AsyncContext` binding used instead of 
                `org.glassfish.jersey.server.internal.process.AsyncContext`                                  
    - (breaking dw 2.0) 
        * Deprecated `Bundle` usages replaced with `ConfigurableBundle`
           (in new dropwizard version `Bundle extends ConfigurableBundle`)
            - Guicey configuration scope `ConfigSope.DropwizardBundle` now use `ConfigurableBundle` class for marking guice 
               bundle scope instead of `Bundle`
        * `dropwizard-bom` now includes only dropwizard modules. All 3rd party dependencies are moved to
            `dropwizard-dependencies` package. So you'll have to update two boms now in order to update dropwizard version.
    - Update hk2 guice-bridge to 2.6.1            
* (breaking) Guicey configuration and lifecycle changes:
    - `GuiceyBundle` contract and behaviour changed to match dropwizard lifecycle: 
        * GuiceyBundle now contains two methods `initialize` and `run` and called according to dropwizard lifecycle.
            Now guicey bundles are complete replacement for dropwizard bundles, but with good interoperability 
            with pure dropwizard bundles 
        * The following guicey initializations were moved into dropwizard configuration phase:
            - Guicey bundles lookup and initialization (to be able to install dropwizard bundles inside guicey bundles)
            - Installers classpath search and instantiation
            - Extensions classpath search and validation (but on run phase it is still possible to disable extensions)
        * Extensions initialization moved outside injector creation scope. It will affect time report and, in case of
           extension installation error, exception will be thrown directly instead of Guice's CreationException.        
        * A lot of guicey lifecycle events obviously changed (and new added) 
            - Add special `ApplicationStarted` event: always fired after complete dropwizard startup. 
                Supposed to be used to simplify diagnostic reporting.
            - Support lifecycle listeners deduplication for correct report behaviour in case of multiple registrations.
               `LinkedHashSet` used as listeners holder, so only proper equals and hashcode methods implementation is required for deduplication          
        * Removed `GuiceyOptions.ConfigureFromDropwizardBundles` option because it's useless with new bundles lifecycle.
            (if required, the same behaviour may be implemented with custom bundles lookup)
    - Removed `GuiceyOptions.BindConfigurationInterfaces` option (interfaces are already bound with `@Config` qualifier)
    - Guicey web installers (`WebInstallersBundle`) enabled by default. 
      `GuiceBundle.builder()#useWebInstallers()` option removed
    - Direct dropwizard bundles support: bundles could be registered directly in main bundle (`GuiceBundle.dropwizardBundles()`)
      or inside guicey bundle (`GuiceyBundle.dropwizardBundles()`). These bundles could be disabled (same as guicey bundles - with 
      `.disableDropwizardBundles()` methods) and are show in reporting.
        * Transitive dropwizard bundles tracking: all dropwizard bundles registered through guicey api are tracked for
          transitive registration with bootstrap proxy. That means that all transitive bundles are shown in reports and 
          any transitive bundle could be disabled (with `.disableDropwizardBundle` or custom predicate). Also, deduplication checks 
          will work (same as for guicey bundles and guice modules).
          Tracking may be disabled with `GuieyOptions.TrackDropwizardBundles` option.  
    - Allow registration of multiple instances for guice modules and guicey bundles 
        (multiple instances of the same class)
        * By default, equal instances of the same type considered duplicate (only one registered).
            So, to grant uniqueness of bundle or module, implement correct equals method.
            For custom cases (when custom equals method is impossible), `DuplicateConfigDetector` may be implemented 
            and registered with `GuiceBundle.Builder#duplicateConfigDetector()` 
        * Legacy behaviour (1 instance per type) could be simulated with: `.duplicateConfigDetector(new LegacyModeDuplicatesDetector())`
          OR method `GuiceBundle.Builder#uniqueItems(Class...)` may be used to specify
          exact items to grant uniqueness for 
        * `ItemId` is now used as identity instead of pure `Class`. ItemId compute object hash string
            and preserve it for instance identification. Class types does not contain hash in id.
            Required because even scopes, represented previously as classes now could be duplicated
            as multiple instances of the same bundle class could be registered. For simplicity,
            ItemId equals method consider class-only id's equal to any type instance id.
        * Add bundle loops detection: as multiple bundle instances allowed loops are highly possible
            Entire bundle chain is provided in exception to simplify fixing loops.
        * Add base classes for unique bundles and modules (with correct equals and hash code implementations):
          `UniqueGuiceyBundle` and `UniqueModule` or `UniqueDropwizardAwareModule` (use class name strings for 
          comparison to correctly detect even instances of classes from different class loaders). 
          Note: no such class for dropwizard bundle because it's useless (if you use guicey - use GuiceyBundle instead 
          and if you need dropwizard bundle - it shouldn't be dependent on guicey classes)     
    - Support extensions recognition from guice modules (jersey1-guice style): 
        * extensions are detected from declaration in specified guice modules 
            (essentially same as classpath scan, but from bindings)            
        * extensions declared in:
            - direct type bindings (all generified or qualified declarations ignored)
            - linked bindings (right part) bind(Something).to(Extension) are also recognized
                (which must also be non qualified)    
        * like in classpath scan `@InvisibleForScanner` prevents recognition
            (or bean may be simply qualified)
        * all extension registration types may work together (classpath scan, manual declaration and binding declaration)    
        * extensions registered directly (or found by classpath scan) and also bound manually in guice module 
            will not conflict anymore (as manual declaration would be detected) and so @LazyBinding workaround is not needed        
        * extensions declared in guice module may be also disabled (guicey will remove binding declaration in this case 
            and all chains leading to this declartion to prevent possible context failures)
        * Transitive gucie modules (installed by other modules) may be disabled with usual `disableModules()`
            (but only if guice bindings analysis is not disabled).
        * enabled by default, but can be disabled with `GuiceyOptions.AnalyzeModules` option
        * `BindingInstaller` interface changed (because of direct guice bindings): 
            it now contains 3 methods for class binding, manual binding validation and reporting
    - Extension classes loaded by different class loaders now detected as duplicate extension registration        
* Guicey hooks, initially supposed to be used for testing only, now considered to be also used for
    diagnostic tools
    - Add guicey hooks lookup from system property `guicey.hooks` as comma-separated list of classes.
    - Add hook aliases support: alias name assumed to be used instead of full class name in system property (`-Dguicey.hooks`).
      Alias registered with `GuiceBundle.builder()#hookAlias()`. All registered aliases are logged at startup.
    - Add diagnostic hook, which enables diagnostic reports and lifecycle logs. 
        Could be enabled with system property: `-Dguicey.hooks=diagnostic` (where diagnostic is pre-registered hook alias) 
        Useful to enable diagnostic logs on compiled (deployed) application.
    - (breaking) Removed hooks recognition on registered GuiceyLifecycleLister (as it was very confusing feature)                         
* Add shared configuration state (for special configuration-time needs like bundles communication). 
    This is required only in very special cases. But such unified place will replace all current and future hacks.
    - Static access by application: `SharedConfigurationState.get(app)` or `SharedConfigurationState.lookup(app, key)`
    - Static access by environment: `SharedConfigurationState.get(env)` or `SharedConfigurationState.lookup(env, key)` 
    - Value access from guicey bundle: `boostrap.sharedState(key, defSupplier)`, `environment.sharedState(key)`
    - Value access from `DropwizardAwareModule`: `sharedState`
    - Hooks can use `GuiceBundle.Builder.withSharedState` to access application state.
    - (breaking) `InjectorLookup` now use global shared state        
        * `clear()` method removed, but `SharedConfigurationState.clear()` could be used instead   
* (breaking) Test support changes
    - Rename test extensions for guicey hooks registration: 
        * `GuiceyConfigurationRule` into `GuiceyHooksRule` 
        * `@UseGuiceyConfiguration` (spock extension) into `@UseGuiceyHooks`
* (breaking) Reporting changes
    - All reports moved into one top-level `debug` package.
    - All guicey reports are now guicey lifecycle listeners
        * `DiagnosticBundle` bundle become `ConfigurationDiagnostic` guicey listener.
            Reporters are no more bound to guice context (they could always be constructed manually).
        * `DebugGuiceyLifecycle` listener renamed into `LifecycleDiagnostic`
        * Guicey reports (listeners) properly implement equals and hashcode in order to 
          use new deduplicatation mechanism and avoid reports duplication (for example,
          if `.printDiagnosticInfo()` would be called multiple times, only one report would be shown;
          but still different configurations will be reported separately (e.g. list `.printDiagnosticInfo()` and 
          `.printAvailableInstallers()` which internally use one listener))
    - Report all diagnostic reports as one log message in order to differentiate `.printDiagnosticInfo()` 
        and `.printAvailableInstallers()` reports when both active      
    - Diagnostic report changes (`.printDiagnosticInfo()`):         
        * Show both dropwizard and guicey bundles together (dropwizard bundles marked with DW)
        * Always show "empty" bundles (bundles without sub registrations) - important for dw bundles
        * Add "-" before ignored or disabled items (to visually differentiate from accepted items)
        * Identify instance deduplication:
            - Instead of registrations count (REG(2)) show exact counter of all registered and accepted items: REG(5/12)
            - Show ignored items even in context where items of the same type were accepted
            - Show exact number of ignored items in context (DUPLICATE(3))
        * Show extension recognized from guice bindings (as sub report)
        * Stats report improved:
            - Show guice internal stat logs in stats diagnostic report (intercept guice logs)
            - Show guicey time by phases (init/run/jersey)
            - Show guice modules analysis stats                      
    - Show installer marker interfaces in `printAvailableInstallers()` report to indicate installer actions
        (installation by type or instance, custom guice or jersey bindings, options support).
    - Detailed lifecycle report (`.printLifecyclePhasesDetailed`) show context data for each event                                                                                                     
    - Add guice bindings report (`printGuiceBindings()` or `printAllGuiceBindings()`)
    - Add guice aop appliance report (`.printGuiceAopMap()`). This report supposed to be used as "a tool" to look exact 
      services and so configurable method version is directly available: 
      `.printGuiceAopMap(new GuiceAopConfig().types(...).methods(...))`
    - Add web mappings report (`.printWebMappings()`): prints all registered servlets and filters
    - Add jersey config report (`.printJerseyConfig`): prints all registered jersey extensions        
* Fix configuration bindings for recursive configuration object declarations (#60)
* Guicey version added into BOM (dependencyManagement section in guicey pom) to avoid duplicate versions declarations
* Java 11 compatibility. Automatic module name (in meta-inf): `dropwizard-guicey.core`
* (breaking) AdminRestBundle moved into ext modules (bundle become guicey bundle, 
    now return 404 instead of 403 on main context for admin-only resources)
* (breaking) Remove useless configuration generic on main bundle: `GuiceBundle.<MyConfig>builder()` must be just `GuiceBundle.builder()`
* InjectorLookup:
    - Add lookup by environment instance: `InjectorLookup.get(environment)` 
    - Add direct lookup for bean instance: `InjectorLookup.getInstance(app, MyBean.class)` (or with environment)      
* Update installers console reporting to use more readable class format: SimpleName   (reduced package)
* Add optional extensions support: optional extension automatically become disabled when no compatible installer found
    Could be registered with new method in main and guicey bundles: `.extensionsOptional`

Migration matrix:

Old class | New class
----------|----------
org.glassfish.hk2.utilities.binding.AbstractBinder | org.glassfish.jersey.internal.inject.AbstractBinder
org.glassfish.hk2.utilities.Binder | org.glassfish.jersey.internal.inject.Binder
org.glassfish.hk2.api.Factory | java.util.function.Supplier
Factory used for Auth (user provider) | java.util.function.Function<ContainerRequest, ?>
org.glassfish.jersey.server.internal.process.AsyncContext | org.glassfish.jersey.server.AsyncContext     
org.glassfish.jersey.server.internal.inject.AbstractValueFactoryProvider | org.glassfish.jersey.server.internal.inject.AbstractValueParamProvider                         
org.glassfish.jersey.server.spi.internal.ValueFactoryProvider | org.glassfish.jersey.server.spi.internal.ValueParamProvider
org.glassfish.hk2.api.InjectionResolver | org.glassfish.jersey.internal.inject.InjectionResolver
io.dropwizard.Bundle | io.dropwizard.ConfiguredBundle (note that interface methods are default now and may not be implemented)
io.dropwizard.util.Size | io.dropwizard.util.DataSize 

### 4.2.2 (2018-11-26)
* Update to guice 4.2.2 (java 11 compatible)
* Update to dropwizard 1.3.7
* Fix inner non static classes detection by classpath scan
* Fix lifecycle debug messages decoration (unicode fix)

### 4.2.1 (2018-07-23)
* Show warning when configuration path's value resolution failed instead of startup fail (#53)
* Add GuiceyOptions.BindConfigurationByPath to be able to disable configuration introspection (for edge cases) (#53)
* Fix unique sub configuration object access (#54) 

### 4.2.0 (2018-06-25)
* Update to guice 4.2.0
    - remove guice-multibindings dependency as it's moved to guice core
* Update to dropwizard 1.3.5    
* Add more disable items (mostly for tests): guicey bundles, guice modules (directly registered) and extensions:
    - Guicey bundles disabled through main builder only: .disableBundles() 
    - Other disable methods available in both main bundle and guicey bundles bootstrap:
        - .disableModules(Class...) - disable guice modules
        - .disableExtensions(Class...) - extensions disabling (for possible replacement) 
        - generic disable method by predicate: .disable(Predicate) (for example, disable all extensions in package or all installed by some bundle etc.)
* Add direct support for guice bindings override (using Modules.override() internally) to main bundle and guicey bundle bootstrap: 
    .modulesOverride(Module...)
    - Add BindingsOverrideInjectorFactory to override already overridden bindings (with modulesOverride) in test (edge case)     
* Add hooks for configuration override in integration tests (#23):
    - New GuiceyConfigurationHook interface: hook receive bundle builder instance after application configuration and so could modify configuration (with new disable* methods)
    - Junit:
        - New rule GuiceyConfigurationRule for hook registration 
    - Spock:
        - New @UseGuiceyConfiguration extension allows base hook definition (in base class)
        - New attribute hooks in @UseGuiceyApp and @UseDropwizardApp extensions to declare test-specific hooks                      
* Add guicey lifecycle events (16 events): provide access to all possible internal state, available at this moment. 
    It may be used to write instance specific features (post processing) or just advanced logging
    - Add new method in main bundle or guicey bundle bootstrap: .listen(GuiceyLifecycleListener...)      
    - Add guicey lifecycle phases reporting methods in main bundle (useful for debugging startup logic):
        - .printLifecyclePhases() - identify configuration stages in console logs 
        - .printLifecyclePhasesDetailed() -  identify lifecycle phases with detailed configuration report (in console logs) 
* Improve options support:
    - Add OptionsAwareModule interface to let guice modules access options
    - Add OptionsMapper helper to simplify mapping of system properties and environment variables in builder.options() 
* Add ability to manage jersey extensions with HK2 by default (#41). 
    It's like using @HK2Managed on all jersey-related beans (resources, filters etc). 
    This is useful, for example, if you get common to jersey resources features like @Context injection.   
    - Add option InstallersOptions.JerseyExtensionsManagedByGuice set to false enable HK2 management by default.
        HK2 bridge must be enabled (GuiceyOptions.UseHkBridge) for HK2-first mode (exception thrown if not).
    - Add @GuiceManaged annotation to mark exceptions in HK2-first mode (when @HK2Managed become useless).
       In guice-first mode this annotation is useless.    
    - Builder shortcut: .useHK2ForJerseyExtensions() to simplify HK2-first mode enabling.
* Guice beans scope-related improvements:
    - Singleton scope is not forced for jersey extensions with explicit scoping annotation 
    - Add option for disabling forced singletons for jersey extensions: InstallerOptions.ForceSingletonForJerseyExtensions
    - Add annotation for guice prototype scope: @Prototype. Useful to declare some jersey extensions as default-scoped even when forced singletons enabled
    - Fix guice request scope delegation support (ServletScopes.transferRequest) for jersey-manager request objects (#49)
* Add Bootstrap object accessible in GuiceyBundle: bootstrap() (return dropwizard bootstrap object)
* Add ConfigScope enum for special scopes description (to not remember special classes).
    - Add shortcut methods in config related apis (Filters, Disables, GuiceyConfigurationInfo)        
* (breaking) Config reporting api changes:
    - Diagnostic report configuration method rename: DiagnosticConfig.printDisabledInstallers renamed to printDisabledItems and affects now all disabled items
    - Diagnostic tree report could hide application scope in ContextTreeConfig.hideScopes(ConfigItems.Application)
* New configuration bindings:
    - Configuration object could be bound as:
        - any class from configuration class hierarchy (as before)
        - any class from hierarchy with @Config qualifier: @Inject @Config Configuration conf
        - interface, implemented by any class in hierarchy with qualifier: @Inject @Config ConfInterface config
        - (Deprecated) GuiceyOptions.BindConfigurationInterfaces: when enabled it would bind configuration with interface (as before),
            but prefer binding interfaces with qualifier (@Config), which is always available.
            Option will be removed in the future versions
        - (Deprecated) bundle's builder.bindConfigurationInterfaces()                 
    - Configuration value (property value) could be bound by path: @Inject @Config("server.serverPush.enabled") Boolean enabledPush
        Or entire sub configuration object: @Inject @Config("server") ServerFactory serverCfg
    - Sub configuration objects could be bound without path if object type appear only once in configuration:
        @Inject @Config ServerFactory serverCfg
    - ConfigurationTree - configuration introspection object is available for direct binding
        - and from GuiceyConfigurationInfo bean: getConfigurationTree()
    - Alternative configuration access:
        - New configuration access methods available inside GuiceyBundle and module (DropwizardAwareModule):
            - configuration(String) - configuration value by path
            - configuration(Class) - unique sub configuration object
            - configurations(Class) - all sub configuration objects with assignable type (on any depth)
            - configurationTree() - access raw introspection data for more complex searches                
    - Reports to see available config bindings (before injector creation for potential problems solving) in main bundle:
        - .printConfigurationBindings() - log all bindings (including dropwizard Configuration) 
        - .printCustomConfigurationBindings() - log only custom bindings (from custom configuration classes)                  


Also, release includes much improved [generics-resolver](https://github.com/xvik/generics-resolver/releases/tag/3.0.0)                      

### 4.1.0 (2017-05-09)
* Update to dropwizard 1.1.0
* Add StartupErrorRule to simplify dropwizard startup error testing
* (breaking) HK2 guice-bridge dependency become optional. New option GuiceyOptions.UseHkBridge could be used 
to enable bridge (#28)  
* Fix NPE when used with JRebel (#29)
* Add binding for jersey javax.ws.rs.container.ResourceInfo (#26)
* Fix loggers for GuiceyAppRule (junit) and @UseGuiceyApp (spock) (#32)
* Fix guava conflict in guicey pom. Make guicey pom usable as BOM.

### 4.0.1 (2016-11-18)
* Update to dropwizard 1.0.5 and fix compatibility (#24)
* Fix guice version conflict for maven (#20)

### 4.0.0 (2016-08-22)
* Update to dropwizard 1.0.0
* (breaking) Remove AdminServletInstaller and AdminFilterInstaller (replaced with new web bundle)
* Add WebInstallersBundle (not installed by default) to install servlet and filters in both main and admin contexts:
    - WebFilterInstaller installs filters annotated with java.servlet.annotation.WebFilter
    - WebServletInstaller installs servlets annotated with java.servlet.annotation.WebServlet
    - WebListenerInstaller installs filters annotated with java.servlet.annotation.WebListener    
* Add general options mechanism. Used to generify core guicey options, provide runtime options access (for bundles and reporting) and allow 3rd party bundles use it's own low-level options.
    - GuiceyBootstrap option(option) method provides access to defined options from bundles
    - Options guice bean provide access to options from guice services
    - Installers could access options by implementing WithOptions interface
    - OptionsInfo guice bean used for accessing options metadata (also accessible through GuiceyConfigurationInfo.getOptions())
    - Options reporting added to DiagnosticBundle
* (breaking) remove GuiceBunldle methods: searchCommands(boolean), configureFromDropwizardBundles(boolean), bindConfigurationInterfaces(boolean) 
    (use either shortcuts without parameters or generic options method instead)
* (breaking) core installers bundle now always installed (for both auto scan and manual modes). May be disabled with GuiceyOptions.UseCoreInstallers option 
* (breaking) configuration info api (GuiceyConfigurationInfo.getData()) changed to use java8 Predicate instead of guava
* (breaking) InjectorLookup changed to use java8 Optional instead of guava    
* Add ability to customize guice filter mapping DispatcherTypes (by default only REQUEST): GuiceyOptions.GuiceFilterRegistration option 
* Add ability to disable guice filter registration and guice servlet modules support (no request and session scopes, but request and response still may be injected in resources) 
* Jersey request specific services UriInfo, HttpHeaders, SecurityContext, Request, ContainerRequest, AsyncContext no longer bound in request scope (scope controlled by HK2)
* Add methods to GuiceBundle builder:
    - option(option, value) - used to specify custom option value
    - options(Map) - used to provide multiple options at once (for custom options lookup mechanisms)
    - printAvailableInstallers() - diagnostic reporting configured to show only available installers (to easily spot available features)
    - useWebInstallers() - shortcut for installing WebInstallersBundle
    - noGuiceFilter() - disables guice filter installation for both contexts and guice servlet modules support  
    - noDefaultInstallers() - disables CoreInstallersBundle automatic installation
    
### 3.3.0 (2016-08-02)
* Update to guice 4.1.0
* Update to dropwizard 0.9.3
* Grant compatibility with guice options: disableCircularProxies, requireExactBindingAnnotations and requireExplicitBindings
* ResourceInstaller looks for @Path on directly implemented interface (#10)
* Fix bundles lookup reporting (correct multiline)
* Fix duplicate extensions installation when registered both manually and by auto scan
* Restrict extension installation to one installer (first matching, according to installers order)
* Improve dropwizard configuration class binding:
    - Complete configuration hierarchy bound (root, all classes between root and Configuration and Configuration itself)
    - (optional) Bind interfaces directly implemented by classes in configuration hierarchy except interfaces from java and groovy packages 
 (it's common to use HasSomeConfig interface convention and now interface may be directly used for binding (when bindConfigurationInterfaces()))
* Add GuiceyBootstrap methods (extend GuiceyBundle abilities):
    - bundles(): add transitive guicey bundles support (to install other guicey bundles from bundle). Duplicate bundles are detected by valueType.
    - application(): returns current application instance
* Rewrite internal configuration mechanism (bundles, installers etc) to generalize it and introduce complete configuration tracking: store registration sources, disabling, used installers and other specific information for each item
    - Add GuiceyConfigurationInfo service to access tracked guicey configuration information (may be used for configuration diagnostic purposes, performing post configuration checks, printing complete configuration tree etc)
    - Add DiagnosticBundle to log configuration items diagnostic information. Log format is configurable. Rendering is externalized and may be re-used (e.g. for web page). 
* Add GuiceBundle builder configuration options:
    - bindConfigurationInterfaces() to enable configuration interface bindings
    - strictScopeControl() is shortcut to enable HK2DebugBundle (to control beans creation scope during development and tests)
    - printDiagnosticInfo() is shortcut to enable DiagnosticBundle with default preset (enable diagnostic logs)
    - shortcut methods for disabled boolean options: searchCommands(), configureFromDropwizardBundles() and bindConfigurationInterfaces()

NOTE: if used FeaturesHolder (internal api bean), now it's renamed to ExtensionsHolder to force upgrade: use new GuiceyConfigurationInfo bean instead (public api)    
    
### 3.2.0 (2016-01-23)
* Clear possible duplicate guicey bundle instances
* Add GuiceyBundleLookup to automatically resolve and install guicey bundles from various sources.
    - Default: check 'guicey.bundles' system property and install bundles described there. May be useful for tests to enable debug bundles.
    - Default: use ServiceLoader mechanism to load declared GuiceyBundle services. Useful for automatic loading of third party extensions.
    - Add builder bundleLookup method to register custom lookup implementation
    - Add builder disableBundleLookup to disable default lookups
    - Default lookup implementation logs all resolved bundles
* Fix JerseyProviderInstaller: prevent HK2 beans duplicate instantiations; fix DynamicFeature support.
* Add HK2DebugBundle. When enabled, checks that beans are instantiated by guice only and annotated with @HK2Managed 
are managed by HK2 only. May be used in tests as extra validation.
* Add JerseyFeatureInstaller (included in code bundle) which installs javax.ws.rs.core.Feature. Useful for low level configuration. 
* Update to dropwizard 0.9
* Revert system exit on guice injector creation error (added in 3.1.1)

### 3.1.1 (2015-11-24)
* Exit on guice injector creation error.
* Add classpath scan packages validation for intersection (to prevent duplicate instances)

### 3.1.0 (2015-09-06)
* JerseyProviderInstaller: 
  - add support for: ParamConverterProvider, ContextResolver, MessageBodyReader, MessageBodyWriter, ReaderInterceptor, WriterInterceptor,
  ContainerRequestFilter, ContainerResponseFilter, DynamicFeature, ApplicationEventListener
  - support multiple extension interfaces on the same bean
* Introduce bundles (GuiceyBundle) to simplify extensions:
  - core installers now registered with CoreInstallersBundle and classpath scan on core installers package is removed
  - builder bundles() method to add guicey bundles
  - builder configureFromDropwizardBundles method enables all registered dropwizard bundles lookup if they implement GuiceyBundle (unified extension mechanism)
* Add admin context rest support (AdminRestBundle)
* Add request scoped beans support in admin context

### 3.0.1 (2015-07-04)
* Add DropwizardAwareModule abstract module to remove boilerplate of using all aware interfaces

### 3.0.0 (2015-04-26)
* Fix HealthCheckInstaller: now installs only NamedHealthCheck classes and ignore other HealthCheck types (which it can't install properly) 
* (breaking) Remove static state from GuiceBundle:
  - GuiceBundle.getInjector method remain, but its now instance specific (instead of static)
  - Injector could be referenced statically using application instance: InjectorLookup.getInjector(app).get() 
  - JerseyInstaller interface signature changed: now install method receives injector instance

### 2.2.0 (2015-04-17)
* Fix ExceptionMapper registration
* Add installers ordering support with @Order annotation. Default installers are ordered now with indexes from 10 to 100 with gap 10 
(to simplify custom installers injection between them)

### 2.1.2 (2015-03-03)
* Spock 1.0 compatibility

### 2.1.1 (2015-01-25)
* Dropwizard 0.8-rc2 compatibility

### 2.1.0 (2015-01-04)
* Add ability to customize injector creation (required by some guice third party modules, like governator). Contributed by [Nicholas Pace](https://github.com/segfly)
* Add spock extensions to use injections directly in specification (like spock-guice do)

### 2.0.0 (2014-11-25)
* Dropwizard 0.8 integration (as result, no more depends on jersey-guice, but depends on guice-bridge(HK2)).
Jersey integration completely rewritten.
* Add JerseyInstaller installer valueType
* Add @LazyBinding annotation, which allows extension not to be registered in guice context (it will be created on first request)

### 1.1.0 (2014-10-23)
* Fix interface generics resolution to support nested generics and moved generics resolution into GenericsUtils instead of FeatureUtils
* Drop java 1.6 compatibility, because dropwizard is 1.7 compatible
* Add junit rule for lightweight testing (run guice without starting jetty)

### 1.0.0 (2014-10-14)
* Add dependency on guice-multibindings
* Installers may choose now from three types of installation (binding, valueType or instance) or combine them.
* Add PluginInstaller: shortcut for multibindings mechanism
* Updated guice (4.0.beta4 -> 4.0.beta5)
* Force singleton for resources
* @Eager renamed to @EagerSingleton and now forces singleton scope for bean
* Add dropwizard style reporting for installed features (like resources or tasks)
* Removed JerseyInjectableProviderInstaller. Now injectable providers must be annotated with @Provider
* Add extensions ordering support using @Order annotation (by default for LifeCycle and Managed installers)
* Add admin context filter and servlet installers
* Rename bundle options: features -> installers, disabledFeatures -> disabledInstallers, beans -> extensions

### 0.9.0 (2014-09-05)

* Initial release
