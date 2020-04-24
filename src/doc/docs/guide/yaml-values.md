# Yaml values

Guicey introspects `Configuration` object instance using jackson serialization api to allow accessing yaml configuration
values by yaml paths or sub-object types.

Introspected configuration:

* Bound directly in guice to inject direct values (by path) or sub objects 
* Accessible from guice modules (with help of `ConfigurationAwareModule` interface) and bundles.

In both cases raw introspection result object is accessible: `ru.vyarus.dropwizard.guice.module.yaml.ConfigurationTree`.
It could be used for config analysis, reporting or something else.

!!! warning
    Jackson will see all properties which either have getter and setter or annotated with `@JsonProperty`. For example,
    ```java
    public class MyConfig extends Configuration {
            
        private String one // visible (getter and setter)
        @JsonProperty
        private String two // visible (annotation)
        private String three // invisible (no getter)

        public void setOne(String one) { ... }
        public String getOne() { ... }

        public void setTwo(String two) { ... }

        public void setThree(String three) { ... }
    }
    ``` 


!!! tip
    To prevent binding of configuration property use `@JsonIgnore` on property *getter*
    ```java
    private String prop
    
    // dropwizard will set value from yaml
    public void setProp(Stirng prop) { ... }

    // property will not be available as path binding
    @JsonIgnore    
    public String getProp() { ... }
    ```

## Unique sub configuration

It is quite common to group configuration properties into sub objects like:

```java
public class MyConfig extends Configuration {
    @JsonProperty AuthConfig auth;
    @JsonProperty DbConfig db;
}
```

Guicey detects such unique objects and provides direct bindings for them:

```java
@Inject @Config AuthConfig auth;
@Inject @Config DbConfig db;
```

This is very useful for re-usable modules, which are not aware of your configuration 
object structure, but require only one sub configuration object:

```java
public class MyConfig extends Configuration {
    @JsonProperty FeatureXConfig featureX;
}
```

Somewhere in module service:

```java
public class FeatureXService {
    @Inject @Config FeatureXConfig featureX; 
}
```

!!! important
    Sub configuration object uniqueness is checked as direct match, so you may have
    ```java
    @JsonProperty SubConfig sub
    @JsonProperty SubConfigExt subExt
    ```
    where `class SubConfigExt extends SubConfig`, but still both objects would be considered unique.
    Whereas
    ```java
    @JsonProperty SubConfig sub1
    @JsonProperty SubConfig sub2
    ```
    will not.
    
!!! tip
    Guicey bundles and guice modules also could use sub configuration objects directly:
    ```java
    GuiceyBootstrap#configuration(SubConfig.class)
    DropwizardAwareModule#configuration(SubConfig.class)
    ```    

## Configuration by path

All visible configuration paths values are directly bindable:

```java
public class MyConfig extends Configuration {
    SubConf sub;
}

public class SubConf {
    String smth;
    List<String> values;
}
```

```java
@Inject @Config("sub") SubConf sub;
@Inject @Config("sub.smth") String smth;
@Inject @Config("sub.values") List<String> values;
```

!!! note
    Path bindings are available even for null values. For example, if sub configuration object
    is null, all it's sub paths will still be available (by class declarations). 
    The only exception is conditional mappin like dropwizard "server" when available paths
    could change, depending on configuration (what configuration class will be used)

!!! note 
    Generified types are bound only with generics (with all available type information).
    If you will have `SubConf<T> sub` in config, then it will be bound with correct generic `SubConfig<String>`
    (suppose generic T is declared as String).
    
Value type, declared in configuration class is used for binding, but there are two exceptions.

If declared type is declared as collection (Set, List, Map) implementation then binding will use
base collection interface:

```java
ArrayList<String> value

@Inject @Config("value") List<String> vlaue;
```

If, for some (unforgivable) reason, property is declared as Object in configuration,
then binding type will depend on value presence:

* `@Config("path") Object val` - when value is null
* `@Config("path") ValueType val` - actual value type, when value is not null       

It is assumed that in such case value would be always present (some sort of property-selected binding, like dropwizard "server").

!!! tip
    You can declare you own additional bindings using `ConfigurationTree` (accessible from guice module), 
    which contains all paths information (including declaration and actual types with generics).

## Introspected configuration

`ConfigurationTree` object provides access for introspected configuration tree:

* `getRootTypes()` - all classes in configuration hierarchy (including interfaces)
* `getPaths()` - all paths (including all steps ("sub", "sub.value"))
* `getUniqueTypePaths()` - paths of unique sub configuration types

Each path item (`ConfigPath`) contains:

* Root path reference ("sub.value" reference "sub")
* Child sub-paths ("sub" reference "sub.value")
* Declaration class (type used in configuration class)
* Value type (type of actual value; when value null - declaration type (but they still could be different for collections))
* Current path name
* Current path value
* Generics for declaration and value types (may be incomplete for value type)
* Custom type marker: contains sub paths or just looks like sub configuration
* Declaration type (class where property was declared - configuration object containing property)

You can traverse up or down from any path (tree structure).

`ConfigurationTree` provides basic search methods (more as usage example):

* `findByPath(String)` - search path by case-insensitive match
* `findAllByType(Class)` - find all paths with assignable declared value
* `findAllFrom(Class<? extends Configuration>)` - find all paths, started in specified configuration class
* `findAllRootPaths()` - get all root paths (1st level paths) 
* `findAllRootPathsFrom(Class<? extends Configuration>)` - all 1st level paths of configuration class
* `valueByPath(String)` - return path value or null if value null or path not exists
* `valuesByType(Class)` - all not null values with assignable type
* `valueByType(Class)` - first not null value with assignable type
* `valueByUniqueDeclaredType(Class)` - value of unique sub conifguration or null if value is null or config is not unique

Paths are sorted by configuration class (to put custom properties upper) and by path name
(for predictable paths order).

## Disable configuration introspection

Introspection process should not fail application startup. In worse case it will show
warning log that property can't be introspected:

```
WARN  [2018-07-23 09:11:13,034] ru.vyarus.dropwizard.guice.module.yaml.ConfigTreeBuilder: Can't bind configuration 
path 'sub.sample' due to IllegalArgumentException: Failed to getValue() with method 
ru.vyarus.dropwizard.guice.yaml.support.FailedGetterConfig#getSample(0 params): null. Enable debug logs to 
see complete stack trace or use @JsonIgnore on property getter.
```

Such warnings could be hidden by using `@JsonIgnore` on property getter.

If this is not enough, or you need to avoid configuration introspection for other reasons,
you can disable introspection completely with option:

```java
GuiceBundle.builder()
    .option(GuiceyOptions.BindConfigurationByPath, false)
    ...
``` 

When introspection disabled, only configuration object would be bound and 
bindings by path would not be available. Note that even `ConfigurationTree` object will not 
contain configuration paths (option completely disables introspection mechanism).
