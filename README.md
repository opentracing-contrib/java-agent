[![Build Status][ci-img]][ci] [![Released Version][maven-img]][maven]

# Java Agent for OpenTracing

**NOTE: This project is no longer being actively developed. Please see the [special agent](https://github.com/opentracing-contrib/java-specialagent) project as an alternative.**

Java Agent for instrumenting Java applications using an OpenTracing compliant Tracer.

The instrumentation is performed in a non-intrusive manner leveraging the [ByteMan project](http://byteman.jboss.org/) to
define a set of rules. These rules can be used to:

* Install a framework integration (e.g. OkHttp, Servlet)

* Define custom rules specific to an application (e.g. create spans to scope important internal units of work,
or add tags to an existing span to identify business relevant properties)

## Usage

The _Java Agent for OpenTracing_ obtains an OpenTracing compliant Tracer using the GlobalTracer from the 
[opentracing util](https://github.com/opentracing/opentracing-java/tree/master/opentracing-util) project.
If a tracer has not been registered, it will then use the
[TracerResolver](https://github.com/opentracing-contrib/java-tracerresolver) mechanism to attempt to access
one from the maven dependencies.

The Java Agent can be used in two ways:

### Prepackaged Agent, Tracer and Dependencies

This approach is to build an uber jar, using the maven assembly or shade plugins, to package together
the `opentracing-agent.jar`, the OpenTracing compliant `Tracer`, any framework integrations, rules, etc.

The important point to remember is that, because the resulting jar will still be used as a javaagent, it needs the
manifest entries copied from the manifest in the `opentracing-agent.jar`.

This approach is useful when wanting to instrument applications where modification of the classpath is not
possible (e.g. executable jars), or wanting to maintain separation between the application and the tracing
mechanism. It also benefits from being able to selective choose the framework integrations (and versions) that
are required.

NOTE: An [issue](https://github.com/opentracing-contrib/java-agent/issues/3) has been created to discuss providing tool support for this option.


### Tracer and Framework Integrations on Classpath

This approach uses the plain `opentracing-agent.jar` provided by this project, and obtains the OpenTracing
Tracer and any required framework integrations, rules, etc. from the classpath.

```java
java -javaagent path/to/opentracing-agent.jar ...
```

## Examples

This section lists examples of using the OpenTracing Java Agent.

* [Spring Boot services](https://github.com/objectiser/java-agent-spring-boot-example) (OpenTracing Tracer: Hawkular APM)


## Creating custom rules

Custom rules are defined using the [ByteMan](http://byteman.jboss.org/) rule format. These rules use
a helper class (_io.opentracing.contrib.agent.OpenTracingHelper_) that provides access to the OpenTracing Tracer,
as well as some additional support capabilities.

The custom rules should be placed in a file on the classpath called `otarules.btm`.

Some example rules are:

```
RULE Custom instrumentation rule sayHello entry
CLASS example.MyClass
METHOD sayHello()
HELPER io.opentracing.contrib.agent.OpenTracingHelper
AT ENTRY
IF TRUE
DO
  getTracer().buildSpan("MySpan").startActive();
ENDRULE
```

The first line defines the name of the rule. The second identifies the target class, although it is also
possibly to specify an interface (using the _INTERFACE_ keyword). The third line identifies the method
name (optionally specifying the parameter types).

The _AT_ clause identifies the point at which the identified method will be instrumented. _ENTRY_ means that
the rule should be applied at the beginning of the method (see ByteMan documentation for other locations).

The _IF_ statement enables a predicate to be defined to guard whether the rule is performed.

The _DO_ clause identifies the actions to be performed when the rule is triggered.

The `getTracer()` method (provided by the _OpenTracingHelper_) is used to access the OpenTracing
compliant `Tracer`.

```
RULE Custom instrumentation rule sayHello normal exit
CLASS io.opentracing.contrib.agent.custom.CustomRuleITest
METHOD sayHello
HELPER io.opentracing.contrib.agent.OpenTracingHelper
BIND
  span : io.opentracing.ActiveSpan = getTracer().activeSpan();
AT EXIT
IF span != null
DO
  span.setTag("status.code","OK");
  span.deactivate();
ENDRULE
```
This rule will trigger _AT EXIT_, so when the method is finished. The _IF_ statement checks whether there
is an active span (assigned to the bound variable `span`), so will only trigger if an active span exists.

The actions performed in this case are to set a tag _status.code_ on the active span, and then finally the
active span needs to be deactivated.


## Supported framework integrations and directly instrumented technologies

### Frameworks

NOTE: Currently the ByteMan rules for installing tracing filters/interceptors into the following frameworks
is contained in this repository. However in the future the aim would be to move the rules to their associated framework
integration projects, meaning that the rules would be detected only when the integration artifact is added to
the classpath.

#### Servlet

Framework integration repo: [Web Servlet Filter](https://github.com/opentracing-contrib/java-web-servlet-filter)

Currently supported containers:

* Jetty

* Tomcat


#### OkHttp

Framework integration repo: [OkHttp](https://github.com/opentracing-contrib/java-okhttp)


#### Mongo Driver

Library integration repo: [Mongo Driver](https://github.com/opentracing-contrib/java-mongo-driver)


## Development
```shell
./mvnw clean install
```

If using JDK9, you will need to set the _MAVEN_OPTS_ as follows:
```shell
MAVEN_OPTS="--add-modules java.se.ee" ./mvnw clean install
```

## Release
Follow instructions in [RELEASE](RELEASE.md)

   [ci-img]: https://travis-ci.org/opentracing-contrib/java-agent.svg?branch=master
   [ci]: https://travis-ci.org/opentracing-contrib/java-agent
   [maven-img]: https://img.shields.io/maven-central/v/io.opentracing.contrib/opentracing-agent.svg?maxAge=2592000
   [maven]: http://search.maven.org/#search%7Cga%7C1%7Copentracing-agent
