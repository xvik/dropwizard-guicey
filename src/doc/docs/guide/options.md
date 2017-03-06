# Options

Options are low level configurations. In contrast to dropwizard configuration (file), which is user specific,
options are set during development and represent developer decisions. Often, options allow to change opinionated default behaviours.

Options are declared with enums. Enums used to naturally group options (also cause pretty reporting). 
Enums must implement Option interface (this makes enum declaration more verbose (because it is impossible to use abstract class in enum),
but provides required option info).

Guicey use options to allow other part to know guice bundle configurations (configured packages to scan, search commands enabling etc) through `GuiceyOptions` enum
(for simplicity, main guicey options usages are already implemented as shortcut methods in guice bundle).
Another use is in web installers to change default behaviour though `InstallersOptions` enum. 

Custom options may be defined for 3rd party bundle or even application. Options is a general mechanism providing configuration and access points with 
standard reporting (part of diagnostic reporting). It may be used as feature triggers (like guicey do), to enable debug behaviour or to specialize
application state in tests (just to name a few).

## Usage
 
Options may be set only in main GuiceBundle using `.option` method. This is important to let configuration parts to see the same values.
For example, if guicey bundles would be allowed to change options then one bundles would see one value and other bundles - different value and,
for sure, this will eventually lead to inconsistent behaviour.

Option could not be set to null. Option could be null only if it's default value is null and custom value not set.
Custom option value is checked for compatibility with option type (from option definition) and error thrown if does not match.
Of course, type checking is limited to top class and generics are ignored (so List<String> could not be specified and so
can't be checked), but it's a compromise between complexity and easy of use (the same as Enum & Option pair).

Options could be accessed by:

* Guicey bundles using `bootstrap.option()` method
* Installer by implementing `WithOptions` interface (or extend `InstallerOptionsSupport`)
* Any guice bean could inject `Options` bean and use it to access options.

Guicey tracks options definition and usage and report all used options as part of [diagnostic reporting](diagnostic.md).
Pay attention that defined (value set) but not used (not consumed) options are marked as NOT_USED to indicate possibly redundant options.

Actual application may use options in different time and so option may be defined as NOT_USE even if its actually "not yet" used.
Try to consume options closer to actual usage to let user be aware if option not used with current configuration. For example,
GuiceyOptions.BindConfigurationInterfaces will not appear in report at all if no custom configuration class used.

## Custom options

Options must be enum and implement Option interface, like this:

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

There is no lookup mechanism implementation, provided by default (for example, like [bundles lookup mechanism](bundles.md#bundle-lookup))
because it's hard to do universal implementation considering wide range of possible values.

But you can write your own lookup, simplified for your case.

If you do, you can use `.options(Map<Enum, Object>)` method to set resolved options (note that contract simplified for just
Enum, excluding Option for simpler usage, but still only option enums must be provided).

```java
    GuiceBundle.builder()
        .options(new MyOptionsLookup().getOptions())
        ...
```

Such mechanism could be used, for example, to change application options in tests or to apply environment specific options.
