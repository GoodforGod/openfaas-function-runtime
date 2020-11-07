## OpenFaaS Function Runtime

OpenFaaS Function runtime with Micronaut DI support with GraalVM native lambda compatibility.

Provides Java native library *OpenfaasLambdaRuntime* with support for Dependency Injection from [Micronaut framework](https://docs.micronaut.io/latest/guide/index.html#ioc).

## Dependencies

Do not forget to add such dependencies in you build for DI and GraalVM support:

```groovy
dependencies {
    annotationProcessor 'io.micronaut:micronaut-inject-java'
    annotationProcessor 'io.micronaut:micronaut-graal'

    compileOnly 'org.graalvm.nativeimage:svm'
}
```

### How To

You just need to implement *Lambda* interface and implement it.

```java
@Singleton
public class MyLambda implements Lambda {

    public IResponse handle(IRequest s) {
        return "response for " + s;
    }
}
```

All will be setup for using it as AWS lambda, you will need just to correctly provide GraalVM properties for image to be build.

### Logging

You can use provided *LambdaLogger* for logging.

```java
@Singleton
public class MyLambda implements Lambda {

    private final LambdaLogger logger;
    
    @Inject
    public MyLambda(LambdaLogger logger) {
        this.logger = logger;
    }

    public IResponse handle(IRequest s) {
        return "response for " + s;
    }
}
```