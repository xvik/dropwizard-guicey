# JDBI 3 integration for Java 8

This is META module (pom only) to simplify jdbi 3 usage with java 8.

### The problem

Since dropwizard 2.0.22, dropwizard-jdbi3 can't work with java 8: [see release notes](https://github.com/dropwizard/dropwizard/releases/tag/v2.0.22) (and requires java 11 now)
This is because of new jdbi3 version depending on caffeine 3. See [caffeine author comment](https://github.com/jdbi/jdbi/issues/1853#issuecomment-819101724).
In short, caffeine breaks java 8 compatibility to fix java 16 and above support.

In order to use it with java 8, caffeine must be downgraded to 2.x. That is exactly what this package do!

### Usage

Use this artifact instead of `guicey-jdbi3`

Maven:

```xml
<dependency>
  <groupId>ru.vyarus.guicey</groupId>
  <artifactId>guicey-jdbi3-jdk8</artifactId>
  <version>{guicey.version}</version>
</dependency>
```

Gradle:

```groovy
implementation 'ru.vyarus.guicey:guicey-jdbi3-jdk8:{guicey.version}'
```

It will bring in [guicey-jdbi3](../guicey-jdbi3) but without caffeine 3.

NOTE: dropwizard-jdbi3 drops java 8 support **in gradle** because it leaks
(through metrics-jdbi3) jdbi3 3.43.1, compiled for java 11, so,
for java 8 compatibility, you'll have to manually force the correct version:

```groovy
    implementation ('org.jdbi:jdbi3-core') {
        version { strictly '3.39.1' }
    }
```


Read [guicey-jdbi3 usage docs](../guicey-jdbi3).