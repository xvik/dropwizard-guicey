# Validation

!!! summary ""
    [Extensions project](https://github.com/xvik/dropwizard-guicey-ext/tree/master/guicey-validation) module


By default, dropwizard allows you to use validation annotations on [rest services](https://www.dropwizard.io/en/stable/manual/validation.html).
This module allows you to use validation annotations the same way on any guice bean method.

Bundle is actually a wrapper for [guice-validator](https://github.com/xvik/guice-validator) project.

## Setup

[![JCenter](https://img.shields.io/bintray/v/vyarus/xvik/dropwizard-guicey-ext.svg?label=jcenter)](https://bintray.com/vyarus/xvik/dropwizard-guicey-ext/_latestVersion)
[![Maven Central](https://img.shields.io/maven-central/v/ru.vyarus.guicey/guicey-validation.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/ru.vyarus.guicey/guicey-validation)

Avoid version in dependency declaration below if you use [extensions BOM](../guicey-bom). 

Maven:

```xml
<dependency>
  <groupId>ru.vyarus.guicey</groupId>
  <artifactId>guicey-validation</artifactId>
  <version>5.2.0-1</version>
</dependency>
```

Gradle:

```groovy
implementation 'ru.vyarus.guicey:guicey-validation:5.2.0-1'
```

See the most recent version in the badge above.


## Usage

By default, no setup required: bundle will be loaded automatically with the bundles lookup mechanism (enabled by default).
So just add jar into classpath and annotations will work.

For example:

```java
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import ru.vyarus.guicey.annotations.lifecycle.PostStartup;

public class SampleBean {    

    private void doSomething(@NotNull String param) {        
    }
    
}
```         

Call `bean.doSomething(null)` will fail with `ConstraintValidationException`.

For more usage examples see [guice-validator documentation](https://github.com/xvik/guice-validator#examples) 

### Explicit mode

By default, validations work in implicit mode: any method containing validation annotations would trigger validation
on call.

If you want more explicitly mark methods requiring validation then register bundle manually:

```java
.bundles(new ValidationBundle()
                    .validateAnnotatedOnly())
```                                                     

Now, only methods annotated with `@ValidateOnExecution` (or all methods in annotated class)
will trigger validation.

If you want, you can use your own annotation:

```java
.bundles(new ValidationBundle()
                .validateAnnotatedOnly(MyAnnotation.class))
```                                                     

### Reducing scope

By default, validation is not applied to resource classes (annotated with `@Path`) because
dropwizard already performs validation there. And rest methods, annotated with `@GET`, `@POST`, etc. 
are skipped (required for complex declaration cases, like dynamic resource mappings or sub resources). 

You can reduce this scope even further:

```java
.bundles(new ValidationBundle()
                    .targetClasses(Matchers.subclassesOf(SomeService.class)
                         .and(Matchers.not(Matchers.annotatedWith(Path.class)))))
```                                                     

Here `SomeService` is excluded from validation (its methods would not trigger validation). 
Note that default condition (not resource) is appended.


Or excluding methods:

```java
.bundles(new ValidationBundle()
                    .targetMethods(Matchers.annotatedWith(SuppressValidation.class)
                         .and(new DirectMethodMatcher())))
```

Now methods annotated with `@SuppressValidation` will not be validated. Note that
`.and(new DirectMethodMatcher())` condition was added to aslo exclude synthetic and bridge methods (jvm generated methods).

!!! note 
    You can verify AOP appliance with guicey `.printGuiceAopMap()` report.                                      