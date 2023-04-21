# Configuration model

All recorder guicey configuration is accessible as:

```java
@Inject GuiceyConfigurationInfo info;
```           

* [Configuration items](#configuration-items)
* [Startup stats](#startup-stats)
* [Option](../options.md) [values and usage info](#options)
* Parsed [configuration values](../yaml-values.md)

## Configuration items

### Identity 

!!! attention
    Configuration items (extensions, installers, bundles etc) are identified with special `ItemId` objects.
    This is important because guicey supports multiple instances of the same type.
    
    `ItemId` object equals behaviour may seem **non intuitive** (see below). 
    
For classes `ItemId` is essentially the same as class: 

```java
ItemId.from(SomeClass.class).equals(ItemId.from(SomeClass.class)) == true
```        

For instances, item id compute object hash (using `System.identityHashCode()`). ItemId could be  
always created from object instance:

```java
ItemId.from(instance).equals(ItemId.from(instance)) == true
```

but for different instances obviously:

```java
ItemId.from(instance).equals(ItemId.from(otherInstance)) == false
```

But instance identity myst also be recognized by class, because otherwise it is 
hard to analyze items, so:

```java
ItemId.from(instance.getClass()).equals(ItemId.from(otherInstance)) == true
```

**Class identity equals all instance identities of the same class**.   

This allows you to use `ItemId.from(MyBundle.class)` to get all instance configs for required bundle types.

### Model

Each configuration item is stored using model object. All model classes are inherited from `ItemInfo`:

Model class | Description   
------------|------------
ItemInfo    | Base class for all models
ClassItemInfo    | Base class for class-based models: extension, installer
InstanceItemInfo    | Base class for instance-based models: bundles, modules 
ExtensionItemInfo | Extension model
InstallerItemInfo | Installer model
ModuleItemInfo | Module model
GuiceyBundleItemInfo | Guicey bundle model
DropwizardBundleItemInfo | Dropwizard bundle model

For example, when you configure extension:

```java
.extensions(MyExtension,class)
```

Guicey will create new instance of `ExtensionItemInfo` to store item registration data. 

For each registered item registration scope is tracked. It could be 

* Application (if item registered in main bundle)
* Guicey bundle class (for items registered in this bundle)
* ClasspathScan (for classes from classpath scan)  
* etc ..

All scopes are described in `ConfigScope` enum.

!!! note
    `ItemId` is also used for scopes, because we may have multiple bundles of the same type
    and so need to differentiate them as different scopes. 

For *class based extensions* only **one model** instance will be created for all registrations
of *the same type*. But all registration scopes will be preserved, so it is possible to know all extension
duplicate points (and you can see it on [diagnostic report](configuration-report.md#configuration-tree))

For *instance based extensions*, different model instances will be created for **different** items.
So if some object was [detected as duplicate](../deduplication.md), it's id will be only added
to original object model (`InstanceItemInfo#getDuplicates()`) to be able to track duplicates.

### Querying model

All raw data is actually available through: `GuiceyConfigurationInfo#getData()`, 
but `GuiceyConfigurationInfo` provides many shortcut methods to simplify raw data querying.  

Data querying is based on java `Predicate` usage. `Filters` class provides common predicate builders.
Many examples of its usage may be found in code.

!!! note
    For some reports it is important to know only types of used configuration items,
    ignoring duplicates. Special helper is included directly inside `ItemId`: `ItemId.typesOnly(...)` 

For example, `getInstallers()` from `GuiceyConfigurationInfo`:

```java
 public List<Class<FeatureInstaller>> getInstallers() {
    return typesOnly(getData().getItems(ConfigItem.Installer, Filters.enabled()));
}
```    

More advanced queries are applied with predicate composition:

```java
 public List<Class<Object>> getExtensionsRegisteredManually() {
    return typesOnly(getData().getItems(ConfigItem.Extension,
            Filters.<ExtensionItemInfo>enabled()
                    .and(it -> it.getRegistrationScopeType().equals(ConfigScope.Application)
                            || it.getRegistrationScopeType().equals(ConfigScope.GuiceyBundle))));
}
```

For exact configuration item type you can always get its configuration model:

```java
@Inject GuiceyConfigurationInfo info;

ItemInfo model = info.getInfo(MyExtension.class)
```                           

For instance-based items, you can receive all models for instances of type:

```java
List<BundleItemInfo> models = info.getInfos(MyBundle.class)
```    

And the last example is if you know exact extension instance and wasn't to get its info:

```java
BundleItemInfo model = info.getData().getInfo(ItemId.from(myBundleInstance))
```

### Instances

Bundles and modules are configured by instance and this instance is stored in configuration model.

For example, to obtain all configured (and not [disabled](../disables.md#disable-guice-modules))  guice modules:

```java
@Inject GuiceConfigurationInfo info;

List<Module> modules = info.getModuleIds().stream()
                           .map(it -> info.getData().<ModuleItemInfo>getInfo(it).getInstance())
                           .collect(Collectors.toList());
```      

Here all used module ids (`ItemId`) obtained. Then complete configuration model loaded for each item
and instance obtained from model. 

!!! note
    It may look overcomplicated to load ids first and only later obtain instances,
    but it is actually a restriction of the model: correct registration order is preserved on
    id level and so this way is the only way to get all instances in registration order.

!!! note
    Direct model object load by id shortcut was not added directly to `GuiceyConfigurationInfo`
    intentionally to avoid mistakes by accidentally using class-based info loading instead of
    id-based (which may lead to configuration items loss in your reporting or analysis).

## Startup stats

Startup stats ar available through: `GuiceyConfigurationInfo#getStats()`.

All available stats are listed in `Stat` enum. Usage examples see in bundles report renderers.

## Options

Options usage info is available through: `GuiceyConfigurationInfo#getOptions()`.

Apart from actual option values, it could tell if custom option value was set and if
this option value was ever queried (by application).

## Configuration tree

[Parsed configuration](../yaml-values.md) object is available through `GuiceyConfigurationInfo#getConfigurationTree()`