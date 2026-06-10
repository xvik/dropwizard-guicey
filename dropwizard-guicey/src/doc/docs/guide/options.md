# Options

Options are low-level configurations. In contrast to Dropwizard configuration (file), which is user-specific,
options are set during development and represent developer decisions. Often, options allow you to change opinionated default behaviors.

Options are declared with enums. Enums are used to naturally group options (and also provide pretty reporting).
Enums must implement the `Option` interface (this makes enum declaration more verbose because it is impossible to use an abstract class in an enum,
but it provides the required option info).

Guicey uses options to share Guice bundle configurations (configured packages to scan, command search enabling, etc.) through the `GuiceyOptions` enum
(for simplicity, the main Guicey option usages are already implemented as shortcut methods in the Guice bundle).
Another use is in web installers to change default behavior through the `InstallersOptions` enum.

Custom options may be defined for a 3rd-party bundle or even the application. Options are a general mechanism providing configuration and access points with
standard reporting (part of [diagnostic reporting](diagnostic/configuration-report.md)). They may be used as feature triggers (like Guicey does), to enable debug behavior, or to specialize
application state in tests (just to name a few).

## Usage

Options may be set only in the main GuiceBundle using the `.option` method. This is important to let configuration parts see the same values.
For example, if Guicey bundles were allowed to change options, then one bundle would see one value and other bundles would see different values and,
for sure, this would eventually lead to inconsistent behavior.

An option cannot be set to null. An option can be null only if its default value is null and no custom value is set.
A custom option value is checked for compatibility with the option type (from the option definition), and an error is thrown if it does not match.
Of course, type checking is limited to the top class and generics are ignored (so `List<String>` could not be specified and therefore
can't be checked), but it's a compromise between complexity and ease of use (the same as the `Enum & Option` pair).

Options could be accessed by:

* Guicey bundles using [`bootstrap.option()`](bundles.md#configuration)
* Installers by implementing the [`WithOptions`](installers.md#options) interface
* Any Guice bean could inject the [`Options`](guice/bindings.md#options) bean and use it to access options.
* A Guice module could access options by implementing the [`OptionsAwareModule`](guice/module-autowiring.md#options) marker interface

Guicey tracks option definition and usage and reports all used options as part of [diagnostic reporting](diagnostic/configuration-report.md).
Note that defined (value set) but not used (not consumed) options are marked as NOT_USED to indicate possibly redundant options.

An application may use options at different times, and so an option may be defined as NOT_USED even if it is actually "not yet" used.
Try to consume options closer to actual usage to let the user be aware if an option is not used with the current configuration. For example,
GuiceyOptions.BindConfigurationInterfaces will not appear in the report at all if no custom configuration class is used.

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

Each enum value declares an option with an exact type and default value. The option type is not limited, but implement a proper `toString()` for a custom object used as an option value.
This is required for pretty reporting, as a simple `toString()` is used for the option value (except collections and arrays, which are rendered as \[\]).

Now you can use an option, for example, in a bean:

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

To provide a custom option value:

```java
    GuiceBundle.builder()
        .option(DoExtraWork, false)
        ...
```

## Options lookup

Guicey provides a simple mapping utility to map properties to system properties, environment variables,
or simply bind from a string (obtained manually somewhere).

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
* `Myoptions.SomeOtherOption` is set from a string (the string could be obtained somewhere else manually)

!!! important
    Missing mappings are ignored: e.g. if a system property or environment variable is not
    defined, the option will remain at its default value (null will not be set!)

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
    You can use string conversion directly somewhere else, if required:
    `StringConverter.convert(TargetType, stringValue)`

An exception is thrown when a type is not supported for conversion. In this case, use a manual converter:

```java
new OptionsMapper()
            .prop("myprop", Myoptions.SomeOption, val -> convertVal(val))
            .map()
```

A converter is actually any `java.util.Function` (here, a lambda with a method call: `::convertVal`).

### System properties

As shown before, you can bind a single system property to an option. But you can also allow
any option to be set with a system property:

```java
new OptionsMapper().props().map()
```

It will bind all properties in the format: `option.enumClasName.enumValue`.
For example, `-Doption.ru.vyarus.dropwizard.guice.GuiceyOptions.UseHkBridge=true`

A different prefix could be used: `.props("myprefix")`

!!! warning
    All properties with the matched prefix must be mappable to an option (the target enum must exist),
    otherwise an error will be thrown.

If any property requires custom value conversion, then bind it *before* with a converter
and it will be ignored during mass mapping by prefix:

```java
new OptionsMapper()
        .prop("option.ru.vyarus.dropwizard.guice.GuiceyOptions.UseHkBridge", 
                GuiceyOptions.UseHkBridge, val - > convert(val))
        .props()
        .map()
```

### Debug

You can enable mapped option printing with `.printMappings()`:

```java
new OptionsMapper()
            .prop("myprop", Myoptions.SomeOption, val -> convertVal(val))
            .printMappings()
            .map()
```

When enabled, all mapped options will be printed to the console (the logger is not used because it is not yet initialized).

Example output:
```text
	env: VAR                   Opts.OptInt = 1
	prop: foo                  Opts.OptStr = bar
	                           Opts.OptBool = true
```

For the mapper:
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

Here, the "VAR2" environment variable and the "foo2" system property were not declared and so were not mapped.

### Custom lookup

You can directly specify a map of options (`.options(Map<Enum, Object>)`) or write your own lookup mechanism:

```java
    GuiceBundle.builder()
        .options(new MyOptionsLookup().getOptions())
        ...
```

!!! note ""
    The `.options()` method contract is simplified for just `Enum`, excluding `Option` for
    simpler usage, but still only option enums must be provided
