# Development tips

!!! summary
    Tips and tricks simplifying development

## Auto reload static resources in IntelliJ IDEA

To enable automatic reload of static resources:

* Go to Help -> Find Action
* Type "Registry".
* Find and mark : “compiler.automake.allow.when.app.running”.
* Go to "Settings -> Build, Execution, Deployment -> Compiler".
* Mark "Build project automatically".

!!! warning
    This is not enabled by default in IDEA because this changes application
    classpath, which may be harmful for some applications. In case of dropwizard
    applications there should be no problems still (only with your custom logic dealing with 
    classpath directly)
    
Now static resources would "hot swap".  

!!! warning
    Note that template engines (freemarker, mustache) may [cache templates](https://www.dropwizard.io/en/latest/manual/views.html#caching)  
