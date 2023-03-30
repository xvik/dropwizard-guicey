### Custom installer implementation sample

There may be many cases when [custom installer](http://xvik.github.io/dropwizard-guicey/5.0.0/guide/installers/#writing-custom-installer) 
need may arise. In all cases installer must replace some boilerplate.

For example,

* [JDBI3](http://xvik.github.io/dropwizard-guicey/5.0.0/extras/jdbi3) use custom installer to install
[repositories](http://xvik.github.io/dropwizard-guicey/5.0.0/extras/jdbi3/#repository) and
[row mappers](http://xvik.github.io/dropwizard-guicey/5.0.0/extras/jdbi3/#row-mapper)
* In [dropwizard-jobs integration example](../integration-dropwizard-jobs) custom installer used for jobs 
registration

In this example, custom installer detects extensions implementing `Marker` interface
and register guice-managed instances in `MarkersCollector` gucie bean (yes, normally such extension
must be done with guice multibindings, but its just for demonstration!)

```java
bootstrap.addBundle(GuiceBundle.builder()      
                // if classpath scan enabled, installer could be detected automatically
                .installers(MarkersInstaller.class)
                .extensions(SampleMarker.class)
```

After startup you can see installer report:

```
INFO  [2019-12-31 08:54:11,994] ru.vyarus.dropwizard.guice.examples.installer.MarkersInstaller: Installed markers = 

    r.v.d.g.e.service.SampleMarker

```

And, with enabled diagnostic report `.printDiagnosticInfo()`, we can see that extension is indeed
recognized by custom installer:

```
    INSTALLERS and EXTENSIONS in processing order = 
        markers              (r.v.d.g.e.installer.MarkersInstaller) 
            SampleMarker                 (r.v.d.g.e.service)       
```

NOTE: installers shine with classpath scan because this way you don't need to even specify 
extensions - installer would detect all classes matched implemented signs.  