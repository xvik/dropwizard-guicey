### Plug-n-play bundle

Guicey allows [automatic bundles loading](http://xvik.github.io/dropwizard-guicey/5.0.0/guide/bundles/#service-loader-lookup),
this way you can do bundles registered automatically as soon as bundle jar appear in classpath.

Example:

* [lifecycle annotations module](http://xvik.github.io/dropwizard-guicey/5.0.0/extras/lifecycle-annotations/)


In most cases, it makes sense to do such bundles with most commonly used configuration.
But user should be able to re-configure bundle if required.

That's why it's better to mark bundle as unique (so only one instance of bundle will be accepted):

```java
public class SampleBundle extends UniqueGuiceyBundle {
    ...
}
```    

It is not necessary to extend `UniqueGuiceyBundle`, 
you can [implement equals and hash code yourself](http://xvik.github.io/dropwizard-guicey/5.0.0/guide/bundles/#de-duplication)


[Bundles lookup](http://xvik.github.io/dropwizard-guicey/5.0.0/guide/bundles/#bundle-lookup) appear after manually registered bundles
registration and so manually registered unique bundle will "override" default.

To activate automatic registration we just need to add service descriptor:

```
META-INF/services/ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
```

With bundle class inside:

```
ru.vyarus.dropwizard.guice.examples.bundle.SampleBundle
``` 

Now, sample application will indicate loaded bundle:

````java
bootstrap.addBundle(GuiceBundle.builder()
        // note: bundle not declared!
        .printDiagnosticInfo()
        .build());
````     

```
    BUNDLES = 
        CoreInstallersBundle         (r.v.d.g.m.installer)      
            WebInstallersBundle          (r.v.d.g.m.installer)      
        SampleBundle                 (r.v.d.g.e.bundle)         *LOOKUP
```


To override default bundle configuration, it must be registered manually:

```java
bootstrap.addBundle(GuiceBundle.builder()
        // override default bundle
        .bundles(new SampleBundle("changed!"))
        .build());
```

NOTE: gucie constant binding used inside to check confguration correctness.

```
    APPLICATION      
    │   
    ├── SampleBundle                 (r.v.d.g.e.bundle)         
    │   └── module     SampleBundle$1               (r.v.d.g.e.bundle)         
    │   
    └── BUNDLES LOOKUP
        └── -SampleBundle                (r.v.d.g.e.bundle)         *DUPLICATE
```    

Here you can see that bundle from lookup was considered as duplicate, so only user-registered 
bundle used.
