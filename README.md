# Orange Mathoms: Logging

This Java library - developed by the Orange Software Experts community - packages several [mathoms](http://tolkiengateway.net/wiki/Mathoms) 
related to logging that are used here and there in our projects.

They are mostly relying on [JEE](https://en.wikipedia.org/wiki/Java_Platform,_Enterprise_Edition),
[SLF4J](https://www.slf4j.org/), [Logback](https://logback.qos.ch/) and 
[logstash-logback-encoder](https://github.com/logstash/logstash-logback-encoder).


## License

This code is under [Apache-2.0 License](LICENSE.txt)


## Table of content

* [Including it in your project](#including)
* [Enrich logs with unique request IDs](#requestIds)
* [Enrich logs with user IDs](#userIds)
* [Enrich logs with session IDs](#sessionIds)
* [Enrich stack traces with unique signatures](#stackTraceSign)
* [Demo application](#demo)



<a name="including"/>

## Include it in your project

Maven style (`pom.xml`):

```xml
<repositories>
  <!-- add the Orange bintray repository -->
  <repository>
    <id>bintray-orange</id>
    <url>http://dl.bintray.com/orange-opensource/maven</url>
    <releases>
      <enabled>true</enabled>
    </releases>
    <snapshots>
      <enabled>false</enabled>
    </snapshots>
  </repository>
</repositories>

<dependencies>
  ...
  <dependency>
    <groupId>com.orange.common</groupId>
    <artifactId>orange-mathoms-logging</artifactId>
    <version>1.0.0</version>
  </dependency>
  ...
</dependencies>
```



<a name="requestIds"/>

## Enrich logs with unique request IDs

### Why

If you're relying on Spring Boot and building a microservices application, our obvious advice is to use [Spring Cloud Sleuth](https://cloud.spring.io/spring-cloud-sleuth/)
to enable distributed tracing and logs correlation.

But if you are in a simpler context (monolothic app) or not using Spring Boot, you might be interested by the 
[RequestIdFilter](src/main/java/com/orange/common/logging/web/RequestIdFilter.java),
a servlet filter, that enriches the logging context (SLF4J's [Mapped Diagnostic Context](https://logback.qos.ch/manual/mdc.html)) 
with a unique request ID.
 
This unique request ID is either *retrieved* from the incoming request's headers (e.g. using an Apache Http server with 
[mod_unique_id](https://httpd.apache.org/docs/current/en/mod/mod_unique_id.html) or any equivalent alternative),
or *generated* if not present.


Notice that you can also *propagate* the request ID when calling other servers by using the [HttpRequestHandlerWithMdcPropagation](src/main/java/com/orange/common/logging/web/HttpRequestHandlerWithMdcPropagation.java)
component (see JavaDoc for more details).

> :warning: The [RequestIdFilter](src/main/java/com/orange/common/logging/web/RequestIdFilter.java)
> has to be installed *as early as possible* in the filters chain, to enrich all subsequent logs with the request ID.

### Configuration

The *incoming request header name*, *MDC key* and *request attribute name* have default values, but can be configured either
programmatically, with filter init parameters, or Java properties:
 
parameter | Java property | filter init param | default value
--------- | ------------- | ----------------- | -------------
request header name    | `slf4j.tools.request_filter.header` | `header` | `X-Track-RequestId`
MDC key                | `slf4j.tools.request_filter.mdc`            | `mdc`            | `requestId`
request attribute name | `slf4j.tools.request_filter.attribute`      | `attribute`      | `track.requestId`

### Example (the Spring Boot way)

Using Spring Boot, a servlet filter can be easily installed as a `@Bean` of type `javax.servlet.Filter` in your Spring Boot application.

Example:

```java
/**
 * Install {@link RequestIdFilter} on every request
 */
@Bean
public Filter requestIdFilter() {
  RequestIdFilter filter = new RequestIdFilter();
  // override the default incoming request header name
  filter.setHeaderName("UNIQUE_ID");
  return filter;
}
```

### Example (the web.xml way)

If you're not relying on Spring Boot, you can anyway use the [RequestIdFilter](src/main/java/com/orange/common/logging/web/RequestIdFilter.java)
filter by declaring it in your `web.xml` descriptor.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app>

  <!-- filter declaration with init params -->
  <filter>
    <filter-name>RequestIdFilter</filter-name>
    <filter-class>com.orange.experts.utils.logging.web.RequestIdFilter</filter-class>
    <init-param>
      <!-- example: request id is passed by Apache mod_unique_id -->
      <param-name>header</param-name>
      <param-value>UNIQUE_ID</param-value>
    </init-param>
  </filter>

  <!-- filter mapping -->
  <filter-mapping>
    <filter-name>RequestIdFilter</filter-name>
    <url-pattern>/*</url-pattern>
  </filter-mapping>
</web-app>
```



<a name="userIds"/>

## Enrich logs with user IDs

### Why

For applications managing users authentication, tagging logs with user IDs enables filtering in one click all logs related to a single user, or 
checking very easily if a given error happens more often on some users of if it's evenly distributed.

This can be done with [PrincipalFilter](src/main/java/com/orange/common/logging/web/PrincipalFilter.java),
a servlet filter, that adds the authenticated `java.security.Principal` name to the logging context 
(SLF4J's  [Mapped Diagnostic Context](https://logback.qos.ch/manual/mdc.html)).

> :warning: The [PrincipalFilter](src/main/java/com/orange/common/logging/web/PrincipalFilter.java)
> has to be installed *after* the authentication filter in the filters chain, so that the authentication context is set.

### Configuration

If the principal name is a personal user information (such as login or email address), it is recommended not to add it 
"as-is" to the logging context, but generate a hash of it.

This filter allows configuring a hashing algorithm. Supported values are:
- `none`: principal name is added unchanged (default),
- `hashcode`: an heaxadecimal representation of the principal name hashcode,
- any other: shall refer to a valid message digest algorithm.

The *hashing algorithm*, *MDC key* and *request attribute name* have default values, but can be configured either
programmatically, with filter init parameters, or Java properties:
 
parameter | Java property | filter init param | default value
--------- | ------------- | ----------------- | -------------
hashing algorithm      | `slf4j.tools.principal_filter.hash_algorithm` | `hash_algorithm` | `none`
MDC key                | `slf4j.tools.principal_filter.mdc`            | `mdc`            | `userId`
request attribute name | `slf4j.tools.principal_filter.attribute`      | `attribute`      | `track.userId`


### Example (the Spring Boot way)

Using Spring Boot, a servlet filter can be easily installed as a `@Bean` of type `javax.servlet.Filter` in your Spring Boot application.

Example:

```java
/**
 * Install {@link PrincipalFilter} on every request
 */
@Bean
public Filter principalFilter(@Value("${logging.principal.hash_algo}") String hashAlgorithm) throws NoSuchAlgorithmException {
  PrincipalFilter filter = new PrincipalFilter();
  // set hashing algorithm through Spring Boot config
  filter.setHashAlgorithm(hashAlgorithm);
  return filter;
}
```

### Example (the web.xml way)

If you're not relying on Spring Boot, you can anyway use the [PrincipalFilter](src/main/java/com/orange/common/logging/web/PrincipalFilter.java)
filter by declaring it in your `web.xml` descriptor.

Example:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app>

  <!-- filter declaration with init params -->
  <filter>
    <filter-name>PrincipalFilter</filter-name>
    <filter-class>com.orange.common.logging.web.PrincipalFilter</filter-class>
    <init-param>
      <!-- example: SHA1 hashed principal -->
      <param-name>hash_algorithm</param-name>
      <param-value>SHA-1</param-value>
    </init-param>
  </filter>

  <!-- filter mapping -->
  <filter-mapping>
    <filter-name>PrincipalFilter</filter-name>
    <url-pattern>/*</url-pattern>
  </filter-mapping>
</web-app>
```


<a name="sessionIds"/>

## Enrich logs with session IDs

### Why

For stateful applications (i.e. managing JEE sessions), tagging logs with session IDs enables filtering in one click all 
logs related to a single session.
It may help - for instance - understand the user journey within his session.

This can be done with [SessionIdFilter](src/main/java/com/orange/common/logging/web/SessionIdFilter.java)
a servlet filter, that adds the current JEE session ID to the logging context 
(SLF4J's  [Mapped Diagnostic Context](https://logback.qos.ch/manual/mdc.html)).


### Configuration

By default, the MDC key used to store the session ID is called `sessionId`, but it may be configured
either programmatically, with filter init parameters, or Java properties:

parameter | Java property | filter init param | default value
--------- | ------------- | ----------------- | -------------
MDC key   | `slf4j.tools.session_filter.mdc`  | `mdc`            | `sessionId`


### Example (the Spring Boot way)

Using Spring Boot, a servlet filter can be easily installed as a `@Bean` of type `javax.servlet.Filter` in your Spring Boot application.

Example:

```java
/**
 * Install {@link SessionIdFilter} on every request
 */
@Bean
public Filter sessionIdFilter() {
  return new SessionIdFilter();
}
```

### Example (the web.xml way)

If you're not relying on Spring Boot, you can anyway use the [SessionIdFilter](src/main/java/com/orange/common/logging/web/SessionIdFilter.java)
filter by declaring it in your `web.xml` descriptor.

Example:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app>

  <!-- filter declaration with init params -->
  <filter>
    <filter-name>SessionIdFilter</filter-name>
    <filter-class>com.orange.common.logging.web.SessionIdFilter</filter-class>
  </filter>

  <!-- filter mapping -->
  <filter-mapping>
    <filter-name>SessionIdFilter</filter-name>
    <url-pattern>/*</url-pattern>
  </filter-mapping>
</web-app>
```


<a name="stackTraceSign"/>

## Enrich stack traces with unique signatures

### Why

It is an easy way to track the error from the client (UI and/or API) to your logs, count their frequency, make sure a 
problem has been fixed for good...

The idea is to generate a short, unique ID that identifies your stack trace.

Example:

```text
#07e70d1e> com.xyz.MyApp$MyClient$MyClientException: An error occurred while getting the things
    at com.xyz.MyApp$MyClient.getTheThings(MyApp.java:26)
    at com.xyz.MyApp.test_logging(MyApp.java:16)
    at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
    at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
    at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
    at java.lang.reflect.Method.invoke(Method.java:498)
    at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:47)
    at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
    ...
Caused by: #393b506a> com.xyz.MyApp$HttpStack$HttpError: I/O error on GET request for http://dummy/things
    at com.xyz.MyApp$HttpStack.get(MyApp.java:40)
    at com.xyz.MyApp$MyClient.getTheThings(MyApp.java:24)
    ... 23 common frames omitted
Caused by: #d6db326f> java.net.SocketTimeoutException: Read timed out
    at com.xyz.MyApp$HttpStack.get(MyApp.java:38)
    ... 24 common frames omitted```
```

### How

This is done thanks to the [CustomThrowableConverterWithHash](src/main/java/com/orange/common/logging/logback/CustomThrowableConverterWithHash.java) component .

Additionally, when pushing logs into JSON native format, you may also use the custom [StackHashJsonProvider](src/main/java/com/orange/common/logging/logback/StackHashJsonProvider.java)
provider, that adds the stack trace signature (hash) as a separate field, for building advanced Kibana dashboards.

Both are installed and configured in Logback configuration files:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!-- application logging configuration to ship logs directly to Logstash -->
<configuration>
  <appender name="TCP" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
    <!-- remote Logstash server -->
    <remoteHost>${logstash_host}</remoteHost>
    <port>${logstash_port}</port>
    <encoder class="net.logstash.logback.encoder.LogstashEncoder">
      <!-- computes and adds a 'stack_hash' field on errors -->
      <provider class="com.orange.common.logging.logback.StackHashJsonProvider"/>
      <!-- enriches the stack trace with unique hash -->
      <throwableConverter class="com.orange.common.logging.logback.CustomThrowableConverterWithHash" />
    </encoder>
  </appender>
  
  <logger name="my.base.package" level="DEBUG" />
  
  <root level="INFO">
    <appender-ref ref="TCP" />
  </root>
</configuration>
```



<a name="demo"/>

## Demo application

Most of those tools are used in a live demo application based on Spring Boot _(coming soon on GitHub)_.