/*
 * Copyright (C) 2017 Orange
 *
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE.txt' in this package distribution
 * or at 'http://www.apache.org/licenses/LICENSE-2.0'.
 */
package com.orange.common.logging.logback;

import ch.qos.logback.classic.pattern.ThrowableHandlingConverter;
import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.CoreConstants;
import com.orange.common.logging.utils.StackHasher;

import java.util.Deque;

/**
 * {@link ThrowableHandlingConverter} that computes and prepends a signature
 * hash in the stack trace
 * <p>
 * Requires SLF4J as the logging facade API.
 * <p>
 * <h2>overview</h2>
 * <p>
 * With a log management system such as ELK, this will help you track an
 * incident:
 * <ul>
 * <li>you may join the error signature to the end user (as "technical details"
 * attached to the error message),
 * <li>from this signature, retrieve in instants the complete stack trace,
 * <li>with this unique signature you will even be able to see the error history
 * (when it occurred for the first time, number of occurrences, frequency, ...)
 * </ul>
 * <p>
 * Example of stack with inlined signatures:
 * <p>
 * <pre style="font-size: small">
 * <span style="font-weight: bold; color: #FF5555">#d39b71d7</span>&gt; my.project.core.api.stream.StreamStoreError: An error occured while loading stream 2ada5bc3cf29411fa183546b13058264/5fe770f915864668b235031b23dd9b4a
 * at my.project.front.business.stream.IssStreamStore.getAsStream(IssStreamStore.java:305)
 * at my.project.front.controller.api.pub.DataController.queryValues(DataController.java:232)
 * at my.project.front.controller.api.pub.DataController$$FastClassBySpringCGLIB$$a779886d.invoke(<generated>)
 * at org.springframework.cglib.proxy.MethodProxy.invoke(MethodProxy.java:204)
 * at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.invokeJoinpoint(CglibAopProxy.java:708)
 * at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:157)
 * at com.ryantenney.metrics.spring.MeteredMethodInterceptor.invoke(MeteredMethodInterceptor.java:45)
 * ... 53 common frames omitted
 * Caused by: <span style="font-weight: bold; color: #FF5555">#4547608c</span>&gt; org.springframework.web.client.ResourceAccessException: I/O error on GET request for "https://api.iss-int.isaservicefor.me/api/v1/datasources/2ada5bc3cf29411fa183546b13058264/streams/5fe770f915864668b235031b23dd9b4a/values?pagesize=1&pagenumber=1&search=metadata.device%3Dnetatmo%3ANAMain%4070%3Aee%3A50%3A12%3Aef%3Afa":Read timed out; nested exception is java.net.SocketTimeoutException: Read timed out
 * at org.springframework.web.client.RestTemplate.doExecute(RestTemplate.java:561)
 * at org.springframework.web.client.RestTemplate.execute(RestTemplate.java:521)
 * at my.project.iss.IssClient.getValuesAsStream(IssClient.java:262)
 * at my.project.front.business.stream.IssStreamStore.getAsStream(IssStreamStore.java:285)
 * ... 91 common frames omitted
 * Caused by: <span style="font-weight: bold; color: #FF5555">#7e585656</span>&gt; java.net.SocketTimeoutException: Read timed out
 * at java.net.SocketInputStream.socketRead0(Native Method)
 * at java.net.SocketInputStream.read(SocketInputStream.java:152)
 * at sun.security.ssl.InputRecord.read(InputRecord.java:480)
 * at com.sun.proxy.$Proxy120.receiveResponseHeader(Unknown Source)
 * at org.springframework.web.client.RestTemplate.doExecute(RestTemplate.java:545)
 * ... 94 common frames omitted
 * </pre>
 * <p>
 * <h2>logback configuration</h2>
 * <p>
 * <h3>using CustomThrowableConverterWithHash with any logback appender</h3>
 * <p>
 * <pre style="font-size: medium">
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 * &lt;configuration&gt;
 * <p>
 * &lt;!-- using the CustomThrowableConverterWithHash with mostly any logback appender --&gt;
 * &lt;!-- 1: define "%sEx" as a conversion rule involving --&gt;
 * &lt;conversionRule conversionWord="sEx" converterClass="com.orange.experts.utils.logging.logback.CustomThrowableConverterWithHash" /&gt;
 * <p>
 * &lt;appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender"&gt;
 * &lt;layout class="ch.qos.logback.classic.PatternLayout"&gt;
 * &lt;!-- 2: use the "%sEx" rule in the layout pattern --&gt;
 * &lt;Pattern&gt;%d{HH:mm:ss.SSS} %-5level %logger [%thread:%X{requestId:--}] - %msg%n%sEx&lt;/Pattern&gt;
 * &lt;/layout&gt;
 * &lt;!-- rest of your config ... --&gt;
 * &lt;/appender&gt;
 * <p>
 * &lt;!-- rest of your config ... --&gt;
 * &lt;root level="INFO"&gt;
 * &lt;appender-ref ref="STDOUT" /&gt;
 * &lt;/root&gt;
 * <p>
 * &lt;/configuration&gt;
 * </pre>
 * <p>
 * <h3>using CustomThrowableConverterWithHash with logstash-logback-appender</h3>
 * <p>
 * <pre style="font-size: medium">
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 * &lt;configuration&gt;
 * <p>
 * &lt;!-- using the CustomThrowableConverterWithHash with any appender from logstash-logback-appender (even simpler!) --&gt;
 * &lt;appender name="LOGSTASH" class="net.logstash.logback.appender.LogstashSocketAppender"&gt;
 * &lt;throwableConverter class="CustomThrowableConverterWithHash" /&gt;
 * &lt;!-- rest of your config ... --&gt;
 * &lt;/appender&gt;
 * <p>
 * &lt;!-- rest of your config ... --&gt;
 * &lt;root level="INFO"&gt;
 * &lt;appender-ref ref="LOGSTASH" /&gt;
 * &lt;/root&gt;
 * <p>
 * &lt;/configuration&gt;
 * </pre>
 *
 * @author pismy
 * @see StackHasher
 */
public class CustomThrowableConverterWithHash extends ThrowableProxyConverter {

    @Override
    protected String throwableProxyToString(IThrowableProxy tp) {
        StringBuilder sb = new StringBuilder(BUILDER_CAPACITY);

        // custom: compute stack trace hashes
        Deque<String> hashes = null;
        if (tp instanceof ThrowableProxy) {
            hashes = StackHasher.hexHashes(((ThrowableProxy) tp).getThrowable());
        }

        recursiveAppend(sb, null, ThrowableProxyUtil.REGULAR_EXCEPTION_INDENT, tp, hashes);

        return sb.toString();
    }

    private void recursiveAppend(StringBuilder sb, String prefix, int indent, IThrowableProxy tp, Deque<String> hashes) {
        if (tp == null)
            return;
        // custom
        String hash = hashes == null || hashes.isEmpty() ? null : hashes.pop();
        subjoinFirstLine(sb, prefix, indent, tp, hash);
        sb.append(CoreConstants.LINE_SEPARATOR);
        subjoinSTEPArray(sb, indent, tp);
        IThrowableProxy[] suppressed = tp.getSuppressed();
        if (suppressed != null) {
            for (IThrowableProxy current : suppressed) {
                recursiveAppend(sb, CoreConstants.SUPPRESSED, indent + ThrowableProxyUtil.SUPPRESSED_EXCEPTION_INDENT, current, hashes);
            }
        }
        recursiveAppend(sb, CoreConstants.CAUSED_BY, indent, tp.getCause(), hashes);
    }

    private void subjoinFirstLine(StringBuilder buf, String prefix, int indent, IThrowableProxy tp, String hash) {
        ThrowableProxyUtil.indent(buf, indent - 1);
        if (prefix != null) {
            buf.append(prefix);
        }
        subjoinExceptionMessage(buf, tp, hash);
    }

    private void subjoinExceptionMessage(StringBuilder buf, IThrowableProxy tp, String hash) {
        // custom
        if (hash != null) {
            buf.append("#" + hash + "> ");
        }
        buf.append(tp.getClassName()).append(": ").append(tp.getMessage());
    }
}