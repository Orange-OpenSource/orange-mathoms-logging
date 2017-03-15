# Orange Mathoms: Logging

This Java library - developed by the Orange Software Experts community - packages several [mathoms](http://tolkiengateway.net/wiki/Mathoms) 
related to logging that are used here and there in our projects.

They are mostly relying on [JEE](https://en.wikipedia.org/wiki/Java_Platform,_Enterprise_Edition),
[SLF4J](https://www.slf4j.org/), [Logback](https://logback.qos.ch/) and 
[logstash-logback-encoder](https://github.com/logstash/logstash-logback-encoder).

## Usage

Add the following dependency to your project `pom.xml`:

```xml
<dependencies>
	...
	<dependency>
        <groupId>com.orange.common</groupId>
        <artifactId>orange-mathoms-logging</artifactId>
        <version>0.0.1</version>
	</dependency>
	...
</dependencies>
```

## Logging utility classes

* [RequestIdFilter](src/main/java/com/orange/common/logging/web/RequestIdFilter.java) a [JEE](https://en.wikipedia.org/wiki/Java_Platform,_Enterprise_Edition) filter that enriches logs with unique request IDs
* [PrincipalFilter](src/main/java/com/orange/common/logging/web/PrincipalFilter.java) a [JEE](https://en.wikipedia.org/wiki/Java_Platform,_Enterprise_Edition) filter that enriches logs with (authenticated) user IDs
* [SessionIdFilter](src/main/java/com/orange/common/logging/web/SessionIdFilter.java) a [JEE](https://en.wikipedia.org/wiki/Java_Platform,_Enterprise_Edition) filter that enriches logs with (JEE) session IDs
* [CustomThrowableConverterWithHash](src/main/java/com/orange/common/logging/logback/CustomThrowableConverterWithHash.java) a [Logback](https://logback.qos.ch/) component that enriches stack traces with unique hashes
* [StackHashJsonProvider](src/main/java/com/orange/common/logging/logback/StackHashJsonProvider.java) a [logstash-logback-encoder](https://github.com/logstash/logstash-logback-encoder) component that adds the stack trace hash as a Json field

## Demo

Most of those tools are used in a live demo application based on Spring Boot _(coming soon on GitHub)_.