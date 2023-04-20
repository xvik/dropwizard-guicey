# Migration guide

## Dropwizard 2.1

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
