/*
 * Copyright (C) 2017 Orange
 *
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE.txt' in this package distribution
 * or at 'http://www.apache.org/licenses/LICENSE-2.0'.
 */
package com.orange.common.logging.logback;

import ch.qos.logback.classic.pattern.ThrowableHandlingConverter;
import ch.qos.logback.classic.spi.*;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.boolex.EvaluationException;
import ch.qos.logback.core.boolex.EventEvaluator;
import ch.qos.logback.core.status.ErrorStatus;
import com.orange.common.logging.utils.StackElementFilter;
import com.orange.common.logging.utils.ThrowableHasher;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * {@link ThrowableHandlingConverter} that computes and prepends a signature
 * hash in the stack trace
 * <p>
 * Inspired by {@link net.logstash.logback.stacktrace.ShortenedThrowableConverter} from logstash-logback-appender
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
 * &lt;<span style="font-weight: bold; color: #FF5555">#d39b71d7</span>&gt; my.project.core.api.stream.StreamStoreError: An error occured while loading stream 2ada5bc3cf29411fa183546b13058264/5fe770f915864668b235031b23dd9b4a
 * at my.project.front.business.stream.IssStreamStore.getAsStream(IssStreamStore.java:305)
 * at my.project.front.controller.api.pub.DataController.queryValues(DataController.java:232)
 * at my.project.front.controller.api.pub.DataController$$FastClassBySpringCGLIB$$a779886d.invoke(<generated>)
 * at org.springframework.cglib.proxy.MethodProxy.invoke(MethodProxy.java:204)
 * at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.invokeJoinpoint(CglibAopProxy.java:708)
 * at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:157)
 * at com.ryantenney.metrics.spring.MeteredMethodInterceptor.invoke(MeteredMethodInterceptor.java:45)
 * ... 53 common frames omitted
 * Caused by: &lt;<span style="font-weight: bold; color: #FF5555">#4547608c</span>&gt; org.springframework.web.client.ResourceAccessException: I/O error on GET request for "https://api.iss-int.isaservicefor.me/api/v1/datasources/2ada5bc3cf29411fa183546b13058264/streams/5fe770f915864668b235031b23dd9b4a/values?pagesize=1&pagenumber=1&search=metadata.device%3Dnetatmo%3ANAMain%4070%3Aee%3A50%3A12%3Aef%3Afa":Read timed out; nested exception is java.net.SocketTimeoutException: Read timed out
 * at org.springframework.web.client.RestTemplate.doExecute(RestTemplate.java:561)
 * at org.springframework.web.client.RestTemplate.execute(RestTemplate.java:521)
 * at my.project.iss.IssClient.getValuesAsStream(IssClient.java:262)
 * at my.project.front.business.stream.IssStreamStore.getAsStream(IssStreamStore.java:285)
 * ... 91 common frames omitted
 * Caused by: &lt;<span style="font-weight: bold; color: #FF5555">#7e585656</span>&gt; java.net.SocketTimeoutException: Read timed out
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
 * @see ThrowableHasher
 */
public class CustomThrowableConverterWithHash extends ThrowableHandlingConverter {

    private static final int BUILDER_CAPACITY = 2048;
    private static final String ELLIPSIS = "...";
    public static final String UNKNOWN_SOURCE = "Unknown Source";

    /**
     * Patterns used to determine which stacktrace elements to exclude.
     *
     * The strings being matched against are in the form "fullyQualifiedClassName.methodName"
     * (e.g. "java.lang.Object.toString").
     *
     * Note that these elements will only be excluded if and only if
     * more than one consecutive line matches an exclusion pattern.
     */
    private List<Pattern> excludes = new ArrayList<>(5);

    /**
     * True to compute and inline stack hashes.
     */
    private boolean inlineHash = true;

    private StackElementFilter stackElementFilter;

    private ThrowableHasher throwableHasher;

    /**
     * Evaluators that determine if the stacktrace should be logged.
     */
    private List<EventEvaluator<ILoggingEvent>> evaluators = new ArrayList<>(1);
    private AtomicInteger errorCount = new AtomicInteger();

    @Override
    public void start() {
        parseOptions();
        // instantiate stack element filter
        if(excludes == null || excludes.isEmpty()) {
            stackElementFilter = StackElementFilter.onlyWithSourceInfo();
        } else {
            stackElementFilter = StackElementFilter.byPattern(excludes);
        }
        // instantiate stack hasher if "inline hash" is active
        if(inlineHash) {
            throwableHasher = new ThrowableHasher(stackElementFilter);
        }
        super.start();
    }

    /**
     * Parse options if this converter is used as a conversion word
     * <p>
     * Options can be:
     * <ul>
     *     <li>evaluator name - name of evaluators that will determine if the stacktrace is ignored</li>
     *     <li>exclusion pattern - pattern for stack trace elements to exclude</li>
     * </ul>
     */
    private void parseOptions() {
        List<String> options = getOptionList();
        if (options == null) {
            return;
        }
        for (String option : options) {
            @SuppressWarnings("rawtypes")
            Map evaluatorMap = (Map) getContext().getObject(CoreConstants.EVALUATOR_MAP);
            @SuppressWarnings("unchecked")
            EventEvaluator<ILoggingEvent> evaluator = (evaluatorMap != null)
                    ? (EventEvaluator<ILoggingEvent>) evaluatorMap.get(option)
                    : null;

            if (evaluator != null) {
                addEvaluator(evaluator);
            } else {
                addExclude(option);
            }
        }
    }

    @Override
    public String convert(ILoggingEvent event) {
        IThrowableProxy throwableProxy = event.getThrowableProxy();
        if (throwableProxy == null || isExcludedByEvaluator(event)) {
            return CoreConstants.EMPTY_STRING;
        }

        // compute stack trace hashes
        Deque<String> stackHashes = null;
        if(inlineHash && (throwableProxy instanceof ThrowableProxy)) {
            stackHashes = throwableHasher.hexHashes(((ThrowableProxy) throwableProxy).getThrowable());
        }

        /*
         * The extra 100 gives a little more buffer room since we actually
         * go over the maxLength before detecting it and truncating.
         */
        StringBuilder builder = new StringBuilder(BUILDER_CAPACITY);
        recursiveAppend(builder, null, ThrowableProxyUtil.REGULAR_EXCEPTION_INDENT, throwableProxy, stackHashes);
        return builder.toString();
    }

    private void recursiveAppend(StringBuilder sb, String prefix, int indent, IThrowableProxy tp, Deque<String> hashes) {
        if (tp == null) {
            return;
        }
        // custom
        String hash = hashes == null || hashes.isEmpty() ? null : hashes.removeFirst();
        appendFirstLine(sb, prefix, indent, tp, hash);
        appendStackTraceElements(sb, indent, tp);

        IThrowableProxy[] suppressed = tp.getSuppressed();
        if (suppressed != null) {
            for (IThrowableProxy current : suppressed) {
                // stack hashes are not computed/inlined on suppressed errors
                recursiveAppend(sb, CoreConstants.SUPPRESSED, indent + ThrowableProxyUtil.SUPPRESSED_EXCEPTION_INDENT, current, null);
            }
        }
        recursiveAppend(sb, CoreConstants.CAUSED_BY, indent, tp.getCause(), hashes);
    }

    /**
     * Appends the first line containing the prefix and throwable message
     */
    private void appendFirstLine(StringBuilder buf, String prefix, int indent, IThrowableProxy tp, String hash) {
        ThrowableProxyUtil.indent(buf, indent - 1);
        if (prefix != null) {
            buf.append(prefix);
        }
        if (hash != null) {
            buf.append("<#" + hash + "> ");
        }
        buf.append(tp.getClassName())
            .append(": ")
            .append(tp.getMessage())
            .append(CoreConstants.LINE_SEPARATOR);
    }

    /**
     * Appends the frames of the throwable.
     */
    private void appendStackTraceElements(StringBuilder builder, int indent, IThrowableProxy throwableProxy) {
        StackTraceElementProxy[] stackTraceElements = throwableProxy.getStackTraceElementProxyArray();
        int commonFrames = throwableProxy.getCommonFrames();

        boolean appendingExcluded = false;
        int consecutiveExcluded = 0;
        int appended = 0;
        for (int i = 0; i < stackTraceElements.length - commonFrames; i++) {
            StackTraceElementProxy stackTraceElement = stackTraceElements[i];
            if (i <= 1 || isIncluded(stackTraceElement)) {
                /*
                 * We should append this line.
                 *
                 * consecutiveExcluded will be > 0 if we were previously skipping lines based on excludes
                 */
                if (consecutiveExcluded >= 2) {
                    /*
                     * Multiple consecutive lines were excluded, so append a placeholder
                     */
                    appendPlaceHolder(builder, indent, consecutiveExcluded, "frames excluded");
                    consecutiveExcluded = 0;
                } else if (consecutiveExcluded == 1) {
                    /*
                     * We only excluded one line, so just go back and include it
                     * instead of printing the excluding message for it.
                     */
                    appendingExcluded = true;
                    consecutiveExcluded = 0;
                    i -= 2;
                    continue;
                }
                appendStackTraceElement(builder, indent, stackTraceElement);
                appendingExcluded = false;
                appended++;
            } else if (appendingExcluded) {
                /*
                 * We're going back and appending something we previously excluded
                 */
                appendStackTraceElement(builder, indent, stackTraceElement);
                appended++;
            } else {
                consecutiveExcluded++;
            }
        }

        if (consecutiveExcluded > 0) {
            /*
             * We were excluding stuff at the end, so append a placeholder
             */
            appendPlaceHolder(builder, indent, consecutiveExcluded, "frames excluded");
        }

        if (commonFrames > 0) {
            /*
             * Common frames found, append a placeholder
             */
            appendPlaceHolder(builder, indent, commonFrames, "common frames omitted");
        }

    }
    /**
     * Appends a single stack trace element.
     */
    private void appendStackTraceElement(StringBuilder builder, int indent, StackTraceElementProxy step) {
        ThrowableProxyUtil.indent(builder, indent);

        StackTraceElement stackTraceElement = step.getStackTraceElement();

        String fileName = stackTraceElement.getFileName();
        int lineNumber = stackTraceElement.getLineNumber();
        builder.append("at ")
                .append(stackTraceElement.getClassName())
                .append(".")
                .append(stackTraceElement.getMethodName())
                .append("(")
                .append(fileName == null ? UNKNOWN_SOURCE : fileName);

        if (lineNumber >= 0) {
            builder.append(":")
                    .append(lineNumber);
        }
        builder.append(")");

        builder.append(CoreConstants.LINE_SEPARATOR);
    }

    /**
     * Appends a placeholder indicating that some frames were not written.
     */
    private void appendPlaceHolder(StringBuilder builder, int indent, int consecutiveExcluded, String message) {
        ThrowableProxyUtil.indent(builder, indent);
        builder.append(ELLIPSIS)
                .append(" ")
                .append(consecutiveExcluded)
                .append(" ")
                .append(message)
                .append(CoreConstants.LINE_SEPARATOR);
    }

    /**
     * Return true if the stack trace element is included (i.e. doesn't match any exclude patterns).
     */
    private boolean isIncluded(StackTraceElementProxy step) {
        return stackElementFilter.accept(step.getStackTraceElement());
    }

    /**
     * Return true if any evaluator returns true, indicating that
     * the stack trace should not be logged.
     */
    private boolean isExcludedByEvaluator(ILoggingEvent event) {
        for (int i = 0; i < evaluators.size(); i++) {
            EventEvaluator<ILoggingEvent> evaluator = evaluators.get(i);
            try {
                if (evaluator.evaluate(event)) {
                    return true;
                }
            } catch (EvaluationException eex) {
                int errors = errorCount.incrementAndGet();
                if (errors < CoreConstants.MAX_ERROR_COUNT) {
                    addError(String.format("Exception thrown for evaluator named [%s]", evaluator.getName()), eex);
                } else if (errors == CoreConstants.MAX_ERROR_COUNT) {
                    ErrorStatus errorStatus = new ErrorStatus(
                            String.format("Exception thrown for evaluator named [%s]", evaluator.getName()), this, eex);
                    errorStatus.add(new ErrorStatus(
                            "This was the last warning about this evaluator's errors."
                                    + "We don't want the StatusManager to get flooded.",
                            this));
                    addStatus(errorStatus);
                }
            }
        }
        return false;
    }


    public boolean isInlineHash() {
        return inlineHash;
    }

    public void setInlineHash(boolean inlineHash) {
        this.inlineHash = inlineHash;
    }

    protected void setThrowableHasher(ThrowableHasher throwableHasher) {
        this.throwableHasher = throwableHasher;
    }

    public void addExclude(String exclusionPattern) {
        excludes.add(Pattern.compile(exclusionPattern));
    }

    public void setExcludes(List<String> exclusionPatterns) {
        if (exclusionPatterns == null || exclusionPatterns.isEmpty()) {
            this.excludes = new ArrayList<>(5);
        } else {
            this.excludes = new ArrayList<>(exclusionPatterns.size());
            for (String pattern : exclusionPatterns) {
                addExclude(pattern);
            }
        }
    }

    public List<String> getExcludes() {
        List<String> exclusionPatterns = new ArrayList<>(excludes.size());
        for (Pattern pattern : excludes) {
            exclusionPatterns.add(pattern.pattern());
        }
        return exclusionPatterns;
    }


    public void addEvaluator(EventEvaluator<ILoggingEvent> evaluator) {
        evaluators.add(evaluator);
    }

    public void setEvaluators(List<EventEvaluator<ILoggingEvent>> evaluators) {
        if (evaluators == null || evaluators.isEmpty()) {
            this.evaluators = new ArrayList<EventEvaluator<ILoggingEvent>>(1);
        } else {
            this.evaluators = new ArrayList<EventEvaluator<ILoggingEvent>>(evaluators);
        }
    }

    public List<EventEvaluator<ILoggingEvent>> getEvaluators() {
        return new ArrayList<EventEvaluator<ILoggingEvent>>(evaluators);
    }
}