# Migration guide

## Dropwizard 4.0

Migration to jakarta namespace (`jakarta.inject`, `jakarta.servlet`, `jakarta.persistence` instead of `javax.*`).

* [dropwizard upgrade instructions](https://www.dropwizard.io/en/release-4.0.x/manual/upgrade-notes/upgrade-notes-4_0_x.html)

Guice 7.0 [drops javax.* support completely](https://github.com/google/guice/wiki/Guice700), so you should either migrate
to guice annotations (like `com.google.guice.Inject`) or use jakarta annotations (like `jakarta.inject.Inject`).

If you're upgrading from dropwizard 2.1 it is recommended to perform step-by-step migration (due to many breaking changes):

* guicey 5.8.1 - dropwizard 2.1, changed guicey project structure (same as in guicey 6)
* guicey 6.0.1 - dropwizard 3 (changed core dropwizard packaged)
* guicey 7.0.0 - dropwizard 4, guice 7 

There might be problems with 3rd party guice libraries still using javax annotations - they would not work as planned
if `javax.inject` annotations used. If possible, migrate such libraries to jakarta namespace or, at least,
use guice native annotations (so library could work with all guice versions).

As the last option, there is a [gradle plugin](https://github.com/nebula-plugins/gradle-jakartaee-migration-plugin)
for automatic conversion of project dependencies from javax to jakarta. This way, application started from gradle project
would use repackaged dependencies with correct jakarta namespace. Application delivery would also contain 
custom (repackaged) jars.

Using this plugin, I did initial [automatic guice migration](https://github.com/xvik/guice-jakartaee), (appeared before official
guice 7 with native jakarta support). You can use this project as an example of 3rd party library
repackage.


## Dropwizard 3.0

Java 8 support dropped! Many core packages were changed so there might be problems with 3rd party modules. 

* [dropwizard upgrade instructions](https://www.dropwizard.io/en/release-4.0.x/manual/upgrade-notes/upgrade-notes-3_0_x.html)

* Guicey core was merged with ext modules to unify versioning. 
* Examples repository was also merged into the [main repository](https://github.com/xvik/dropwizard-guicey/tree/master/examples)
* There is only one BOM now: `ru.vyarus.guicey:guicey-bom`. 
* Dropwizard-guicey POM is not a BOM anymore (removing ambiguity). POM simplified by using direct exclusions instead of relying on BOM.

!!! note
    Guicey 5.8.0 (for dropwizard 2.1) applies the same project structure as in guicey 6 (dropwizard 3) and
    so you can use it as the first migration step.

## Dropwizard 2.1

* [dropwizard upgrade notes](https://www.dropwizard.io/en/release-4.0.x/manual/upgrade-notes/upgrade-notes-2_1_x.html)

Since dropwizard 2.1.0 [jackson blackbird](https://github.com/FasterXML/jackson-modules-base/tree/jackson-modules-base-2.13.3/blackbird#readme)
[used by default](https://www.dropwizard.io/en/release-2.1.x/manual/upgrade-notes/upgrade-notes-2_1_x.html#jackson-blackbird-as-default)
instead of [afterburner](https://github.com/FasterXML/jackson-modules-base/tree/jackson-modules-base-2.13.3/afterburner#readme).
If you use **java 8** then apply afterburner dependency in order to switch into it:

```
implementation 'com.fasterxml.jackson.module:jackson-module-afterburner:2.13.3'
```

(omit version if guicey or dropwizard if BOM used).
Without it, you'll always see a nasty warning on startup (afterburner is better for java 8, but for java 9+ blackbird should be used)

* [Java 8 issue discussion](https://github.com/xvik/dropwizard-guicey/discussions/226)
* [dropwizard upgrade instructions](https://www.dropwizard.io/en/release-2.1.x/manual/upgrade-notes/upgrade-notes-2_1_x.html)

## Dropwizard 2.0

* [dropwizard upgrade instructions](https://www.dropwizard.io/en/release-2.0.x/manual/upgrade-notes/upgrade-notes-2_0_x.html)
* [guicey migration guide](http://xvik.github.io/dropwizard-guicey/5.0.0/about/release-notes/#migration-guide).
