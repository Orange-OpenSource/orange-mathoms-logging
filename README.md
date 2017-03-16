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
[RequestIdFilter](src/main/java/com/orange/common/logging/web/RequestIdFilter.java)
servlet filter, that generates and enriches logs with a unique request ID.

> :information_source: This filter has several functionalities, such as the ability to *retrieve the unique request ID from the incoming request*
> (in case you are using a front Apache Http server with [mod_unique_id](https://httpd.apache.org/docs/current/en/mod/mod_unique_id.html),
> or any other front server that already generates such an ID), thus implementing end-2-end logs traceability.
>
> :warning: The [RequestIdFilter](src/main/java/com/orange/common/logging/web/RequestIdFilter.java)
> has to be installed *as early as possible* in the filters chain, to enrich all subsequent logs with the request ID.

### How to (the Spring Boot way)

Using Spring Boot, a servlet filter can be easily installed as a `@Bean` of type `javax.servlet.Filter` in your Spring Boot application.

Example:

```java
/**
 * Install {@link RequestIdFilter} on every request
 */
@Bean
public Filter requestIdFilter() {
  return new RequestIdFilter();
}
```

### How to (the web.xml way)

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

This can be done with the [PrincipalFilter](src/main/java/com/orange/common/logging/web/PrincipalFilter.java)
servlet filter, that retrieves the authentication from the standard JEE `java.security.Principal`.

>  :information_source: This filter has several functionalities, such as the ability to *hash the principal name* with 
> any hashing algorithm of your choice, in case the
> principal name is a personal/sensitive info that shall not appear plaintext.

### How to (the Spring Boot way)

Using Spring Boot, a servlet filter can be easily installed as a `@Bean` of type `javax.servlet.Filter` in your Spring Boot application.

Example:

```java
/**
 * Install {@link PrincipalFilter} on every request
 */
@Bean
public Filter principalFilter(@Value("${logging.principal.hash_algo}") String hashAlgorithm) throws NoSuchAlgorithmException {
  PrincipalFilter filter = new PrincipalFilter();
  filter.setHashAlgorithm(hashAlgorithm);
  return filter;
}
```

### How to (the web.xml way)

If you're not relying on Spring Boot, you can anyway use the [PrincipalFilter](src/main/java/com/orange/common/logging/web/PrincipalFilter.java)
filter by declaring it in your `web.xml` descriptor.

> :warning: The [PrincipalFilter](src/main/java/com/orange/common/logging/web/PrincipalFilter.java)
> has to be installed *after* the authentication filter in the filters chain, so that the authentication context is set.

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

This can be done thanks to the [SessionIdFilter](src/main/java/com/orange/common/logging/web/SessionIdFilter.java)
servlet filter .

### How to (the Spring Boot way)

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

### How to (the web.xml way)

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