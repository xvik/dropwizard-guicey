# Options

Options are low level configurations. In contrast to dropwizard configuration (file), which is user specific,
options are set during development and represent developer decisions. Often, options allow to change opinionated default behaviours.

Options are declared with enums. Enums used to naturally group options (also cause pretty reporting). 
Enums must implement `Option` interface (this makes enum declaration more verbose (because it is impossible to use abstract class in enum),
but provides required option info).

Guicey use options to share guice bundle configurations (configured packages to scan, search commands enabling etc) through `GuiceyOptions` enum
(for simplicity, main guicey options usages are already implemented as shortcut methods in guice bundle).
Another use is in web installers to change default behaviour though `InstallersOptions` enum. 

Custom options may be defined for 3rd party bundle or even application. Options is a general mechanism providing configuration and access points with 
standard reporting (part of [diagnostic reporting](diagnostic.md)). It may be used as feature triggers (like guicey do), to enable debug behaviour or to specialize
application state in tests (just to name a few).

## Usage
 
Options may be set only in main GuiceBundle using `.option` method. This is important to let configuration parts to see the same values.
For example, if guicey bundles would be allowed to change options then one bundles would see one value and other bundles - different value and,
for sure, this will eventually lead to inconsistent behaviour.

Option could not be set to null. Option could be null only if it's default value is null and custom value not set.
Custom option value is checked for compatibility with option type (from option definition) and error thrown if does not match.
Of course, type checking is limited to top class and generics are ignored (so `List<String>` could not be specified and so
can't be checked), but it's a compromise between complexity and easy of use (the same as `Enum & Option` pair).

Options could be accessed by:

* Guicey bundles using [`bootstrap.option()`](bundles.md#options)
* Installer by implementing [`WithOptions`](installers.md#options) interface 
* Any guice bean could inject [`Options`](guice/bindings.md#options) bean and use it to access options.
* Guice module could access options by implementing [`OptionsAwareModule`](guice/module-autowiring.md#options) marker interface

Guicey tracks options definition and usage and report all used options as part of [diagnostic reporting](diagnostic.md).
Pay attention that defined (value set) but not used (not consumed) options are marked as NOT_USED to indicate possibly redundant options.

Actual application may use options in different time and so option may be defined as NOT_USE even if its actually "not yet" used.
Try to consume options closer to actual usage to let user be aware if option not used with current configuration. For example,
GuiceyOptions.BindConfigurationInterfaces will not appear in report at all if no custom configuration class used.

## Custom options

Options must be enum and implement `Option` interface, like this:

```java
enum MyOptions implements Option {

    DoExtraWork(Boolean, true),
    EnableDebug(Boolean, false),
    InternalConfig(String[], new String[]{"one", "two", "three"});

    private Class type
    private Object value

    // generic used only to check type - value correctness
    <T> SampleOptions(Class<T> type, T value) {
        this.type = type
        this.value = value
    }

    @Override
    public Class getType() {
        return type
    }

    @Override
    public Object getDefaultValue() {
        return value
    }
}
```

Each enum value declares option with exact type and default value. Option type is not limited, but implement proper toString for custom object used as option value.
This will require for pretty reporting, as simple toString used for option value (except collections and arrays are rendered as \[\]).

Now you can use option, for example, in bean:

```java
import static MyOptions.DoExtraWork;

public class MyBean {
    @Inject Options options;
    
    pulic void someMethod() {
        ... 
        if (options.get(DoExtraWork)) {
            // extra work impl
        }
    }
}
```

To provide custom option value:

```java
    GuiceBundle.builder()
        .option(DoExtraWork, false)
        ...
```

## Options lookup

Guicey provides simple mapping utility to map properties to system properties, environment variables 
or simply bind from string (obtained manually somewhere). 

```java
GuiceBundle.builder()
    ...
    .options(new OptionsMapper()
                    .prop("myprop", Myoptions.SomeOption)
                    .env("STAGE", GuiceyOptions.InjectorStage)
                    .string(Myoptions.SomeOtherOption, "property value")
                    .map()) 
    .build()                
```

Here:

* `Myoptions.SomeOption` could be changed with "myprop" system property (`-Dmyprop=something`)
* `GuiceyOptions.InjectorStage` could be changed with environment variable "STAGE"
* `Myoptions.SomeOtherOption` set from string (string could be obtained somewhere else manually) 

!!! important
    Missed mappings are ignored: e.g. if system property or environment variable is not 
    defined - option will remain with default value (null will not be set!)

### Supported conversions

!!! note ""
    Each option declares required option type

Mapper could automatically convert string to:

* String
* Boolean
* Integer
* Double
* Short
* Byte
* Enum constant: 
    - If option type is exact enum then value must be constant name
    - If option type is generic `Enum` then value must be 'fullEnumClass.constantName'
* Array or any type (from above): values must be separated by comma ("one, two, three")
* EnumSet: value must be comma separated list with fully qualified enum constants ('fullEnumClass.constantName')   

!!! tip
    You can use sting conversion directly somewhere else, if required:
    `StringConverter.convert(TargetType, stringValue)`

Exception is thrown when type is not supported for conversion. In this case use manual converter:

```java
new OptionsMapper()
            .prop("myprop", Myoptions.SomeOption, val -> convertVal(val))
            .map()
```

Converter is actually any `java.util.Function` (here, lambda with method call (`::convertVal`)).

### System properties

As shown before, you can bind single system property to option. But you can allso allow
to set any option with system property:

```java
new OptionsMapper().props().map()
```

It will bind all properties in format: `option.enumClasName.enumValue`.
For example, `-Doption.ru.vyarus.dropwizard.guice.GuiceyOptions.UseHkBridge=true` 

Different prefix could be used: `.props("myprefix")` 

!!! warning
    All properties with matched prefix must be mappable to option (target enum exists),
    otherwise error will be thrown.

If any property requires custom value conversion then bind it *before* with converter
and it will be ignored during mass mapping by prefix:

```java
new OptionsMapper()
        .prop("option.ru.vyarus.dropwizard.guice.GuiceyOptions.UseHkBridge", 
                GuiceyOptions.UseHkBridge, val - > convert(val))
        .props()
        .map()
```

### Debug

You can enable mapped options print with `.printMappings()`:

```java
new OptionsMapper()
            .prop("myprop", Myoptions.SomeOption, val -> convertVal(val))
            .printMappings()
            .map()
```

When enabled, all mapped options will be printed to console (logger is not used becuase it's not yet initialized).

Example output:
```
	env: VAR                   Opts.OptInt = 1
	prop: foo                  Opts.OptStr = bar
	                           Opts.OptBool = true
```

for mapper:
```java
new OptionsMapper()
        .printMappings()
        .env("VAR", Opts.OptInt)
        .env("VAR2", Opts.OptDbl)
        .prop("foo", Opts.OptStr)
        .prop("foo2", Opts.OptShort)
        .string(Opts.OptBool, "true")
        .map()
```

Here "VAR2" env. variable and "foo2" system property wasn't declared and so not mapped. 

### Custom lookup

You can directly specify map of options (`.options(Map<Enum, Object>)`) or write your own lookup mechanism:

```java
    GuiceBundle.builder()
        .options(new MyOptionsLookup().getOptions())
        ...
```

!!! note ""
    `.options()` method contract simplified for just `Enum`, excluding `Option` for 
    simpler usage, but still only option enums must be provided
