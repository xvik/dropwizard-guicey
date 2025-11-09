# Essential JVM Heap Settings

!!! important
    [Original article](https://medium.com/itnext/essential-jvm-heap-settings-what-every-java-developer-should-know-b1e10f70ffd9?sk=24f9f45adabf009d9ccee90101f5519f) (source)

JVM Heap optimization in newer Java versions is highly advanced and container-ready. 
This is great to quickly get an application in production without having to deal with 
various JVM heap related flags. But the default JVM heap and GC settings might surprise 
you. Know them before your first OOMKilled encounter.

!!! tip ""
    You need to be on Java 9+ for anything written below to be applicable. Still on Java 8? 
    Time to upgrade Java or job…

### Running your Java application under the layers of Container or Kubernetes? The environment variable JAVA_TOOL_OPTIONS is your friend

If you are running in a constrained environment with limited access to modify the comand 
`java -jar ...`, don’t worry it is very easy to pass in custom JVM flags. Just set 
the environment variable `JAVA_TOOL_OPTIONS` and it will be automatically picked up by 
the JDK. This is true for OpenJDK and its variants like RedHat. If you are using a 
different JDK, check documentation for an equivalent variable.

You will see a log line as below during startup:

```
Picked up JAVA_TOOL_OPTIONS: -XX:SharedArchiveFile=application.jsa -XX:MaxRAMPercentage=80
```

Be aware that if you have multiple JVM applications running, setting the environment 
variable might affect all of them.

### No idea what heap size or JVM flags are active? Use -XX:+PrintCommandLineFlags

Unless you have explicitly set the `-Xmx/-Xms` flags, you probably have no idea about 
the available heap size. Metrics may give a hint but that is a lagging indicator. 
Set the flag `-XX:+PrintCommandLineFlags` to force the JVM to print all active flags 
at startup.

It would look something like this:

```
-XX:InitialHeapSize=16777216 -XX:MaxHeapSize=858993459 -XX:MaxRAM=1073741824 -XX:MaxRAMPercentage=80.000000 -XX:MinHeapSize=6815736 -XX:+PrintCommandLineFlags -XX:ReservedCodeCacheSize=251658240 -XX:+SegmentedCodeCache -XX:SharedArchiveFile=application.jsa -XX:-THPStackMitigation -XX:+UseCompressedOops -XX:+UseSerialGC
```

This is useful to gain insights on your current JVM setup.

Another flag `-XX:+PrintFlagsFinal` shows every flag including defaults. But it might 
be overkill to include in every application startup. If your Java application is 
wrapped inside a container image, this command is a quick way to see the JVM flags that 
will be applied: `docker run --rm --entrypoint java myimage:latest -XX:+PrintFlagsFinal -version`

### I am applying container memory limits. Do I need to set heap flags?

It depends. For a typical application not requiring optimizations, the default behaviour 
would be fine. JVM will automatically apply a percentage of available memory as maximum 
heap size. Just make sure to leave some space for non-heap stuff, sidecars, agents, etc. 
How much? Read on.

### By default only 25% of available memory is used as max heap!

With many JDK vendors, a container with 1GB memory limit will only get 256 MB of maximum 
heap size. This is due to the default flag `-XX:MaxRAMPercentage=25` set. 
This conservative number made sense back in the non-container days when multiple JVMs would run 
on the same machine. But when running in containers with memory limits set correctly, 
this value can be increased to 60, 70 or even 80% depending on the application’s non-heap
memory usage like byte buffers, page cache etc.

```
> docker run --memory 2g openjdk:24 java -XX:+PrintFlagsFinal -version | grep MaxRAMPercentage
    double MaxRAMPercentage                         = 25.000000                                 {product} {default}
```

### Garbage Collection algorithm changes depending on available memory

Since Java 9, G1 is the default garbage collection algorithm replacing Parallel GC in 
previous versions. But there is a caveat! This applies only if available memory (not heap 
size) is at least 2 GB. Below 2 GB, serial GC is the default algorithm.

```
> docker run --memory 1g openjdk:24 java -XX:+PrintFlagsFinal -version | grep -E "UseSerialGC | UseG1GC"
    bool UseG1GC                                  = false                                     {product} {default}
    bool UseSerialGC                              = true                                      {product} {ergonomic}

> docker run --memory 2g openjdk:24 java -XX:+PrintFlagsFinal -version | grep -E "UseSerialGC | UseG1GC"
    bool UseG1GC                                  = true                                      {product} {ergonomic}
    bool UseSerialGC                              = false                                     {product} {default}
```

This is likely due to the fact that G1 GC carries overhead of metadata and its own bookkeeping 
which outweighs the benefits in low-memory applications.

You can always set your own GC algorithm with flags like `-XX:+UseG1GC` and `-XX:+UseSerialGC`.

### Kubernetes pods memory limit affect heap sizes

The memory limit set on the pod affect the heap size calculations. The memory request 
has no impact. It only affects the scheduling of the pod on a node.

### JVM flag UseContainerSupport is not necessary

Since Java 10+, the JVM flag UseContainerSupport is available and always enabled by default.

```
> docker run --memory 1g openjdk:24 java -XX:+PrintFlagsFinal -version | grep UseContainerSupport
    bool UseContainerSupport                      = true                                      {product} {default}
```

### Common Heap Regions

The JVM heap space is broadly divided into two regions or generations — Young and Old 
generation. The Young generation is further divided into Eden and Survivor space. 
The survivor space consists of two equally divided spaces S0 and S1.

A newly created object is born in the Eden space. If it survives one or two garbage 
collections, it is promoted to the Survivor space. If it survives even more garbage 
collections, it is considered an elder and promoted to the Tenured or Old space.

```
Total heap size = Eden space + Survivor space + Tenured space
```

### Metrics Gotchas for Serial and G1 GC

Typical Heap monitoring view for Serial GC

[![Monitoring 1](../img/jvm/jvm1.webp)](https://channel.io "Typical Heap monitoring view for Serial GC")

When the available memory is less than 2 GB and Serial GC is active, the max sizes of 
Eden, Survivor and Tenured spaces will be fixed. The size of Young generation 
(Eden + Survivor) is determined by `MaxNewSize` which usually defaults to 1/3rd of the 
max heap size. Within the young generation, the sizing of Eden and Survivor is 
determined via `NewRatio` and `SurvivorRatio`. These default to 2 and 8 respectively in 
OpenJDK. Effectively, old generation will be twice the size of young generation and 
the Survivor space is 1/8th the size of Eden space.

```
Heap breakup under 2 GB / Serial GC

Container memory limit = 1 GB
|_ Max heap size = 256 MB (25%)
  |_ Young generation =~ 85 MB (1/3 of heap size)
     |_ Eden space =~ 76 MB (85 * 8/9)
     |_ Survivor space =~ 9 MB (85 * 1/9, S0 = 4.5 MB, S1 = 4.5 MB)
  |_ Old generation =~ 171 MB (max heap size - young generation)
```

These numbers would approximately reflect in the JVM heap metrics.

Typical Heap monitoring view for G1 GC

[![Monitoring 2](../img/jvm/jvm2.webp)](https://channel.io "Typical Heap monitoring view for G1 GC")

The most striking difference in metrics for G1 GC compared to Serial GC is that the 
max sizes of Eden and Survivor spaces show as zero. This is because in G1 GC, the size 
of these spaces are not fixed and is resized after every GC cycle. This can be 
confusing in the metrics as the values are non-zero while max is zero. The flags 
`MaxNewSize`, `NewRatio` and `SurvivorRatio` apply to generational GCs like Serial and 
Parallel only and not G1.

```
Heap breakup over 2 GB / G1 GC

Container memory limit = 2GB
|_ Max heap size = 512 MB (25%)
  |_ Young generation =~ Adaptive
     |_ Eden space =~ Adaptive / -1 as reported by metrics
     |_ Survivor space =~ Adaptive / -1 as reported by metrics
  |_ Old generation =~ Adaptive / 512 MB as reported by metrics
```

### Metaspace and Compressed Class Space

Misleading Metaspace and Compressed Class Space metric

[![Monitoring 3](../img/jvm/jvm3.webp)](https://channel.io "Misleading Metaspace and Compressed Class Space metric")

Outside of heap, an important memory region is the Metaspace which stores information 
about loaded classes, methods, fields, annotations, constants, and JIT code. The size 
of Metaspace is determined by the flag `MaxMetaspaceSize` which is by default unlimited. 
It can use all native memory outside of heap and within the available memory. If 
usage goes beyond this, you would see `java.lang.OutOfMemoryError: Metaspace`. Large 
number of loaded classes will increase the Metaspace usage.

Compressed Class Space stores ordinary object pointers ([oops](https://wiki.openjdk.org/display/HotSpot/CompressedOops)) to Java objects by 
compressing them from 64 to 32-bit offsets thereby saving some valuable memory space. 
More importantly, it is a sub-region of the Metaspace. The metrics report the size of 
Compressed Class space as 1 GB since the flag `CompressedClassSpaceSize` is set to 1 GB 
by default irrespective of available memory. It is not allocated unless needed. But 
since this is a sub-region of Metaspace, setting `MaxMetaspaceSize` is enough.

### Reserved Code Cache

Different regions of the JVM’s code cache

[![Monitoring 4](../img/jvm/jvm4.webp)](https://channel.io "Different regions of the JVM’s code cache")

This is the memory space outside heap that stores the native code generated by 
Just-In-Time (JIT) compiler.

Java source code is compiled into Java binary code which is executed by the JVM. 
JVM interprets the binary code into OS-specific machine code line-by-line upon every 
execution. While this is enough, it would be very slow. The JIT compiler identifies 
hotspots (code paths that are frequently accessed), compiles them into native code 
and stores it in the Reserved Code Cache. The next time the hot code path requires 
execution, no interpretation is needed as the corresponding native code is directly 
invoked.

> Interpreter is like asking a professional translator to translate a phrase in an 
> unknown language into a familiar language every time without learning.
>
> JIT compilation is like learning the frequently used phrases in the unknown language 
> to not rely on the translator all the time.
>
> AOT compilation is like learning the complete language beforehand and never needing 
> the translator.

By default, the code cache is segmented into multiple regions for optimization. 
These regions include `non-nmethods` (unrelated to user code, internal to JIT compiler), 
`non-profiled nmethods` (native methods that have not been profiled yet) and `profiled 
nmethods` (native methods that have been aggressively optimized). The total size of 
reserved code cache is defined via the flag `ReservedCodeCacheSize` and defaults to 
240 MB since Java 10.       

### Conclusion
While there is much more to study in this area, I consider the things listed here 
as must-know for every Java developer. The next time you encounter OOM errors, 
you can check the JVM metrics and be able to immediately gather relevant information.