package com.orange.common.logging.logback;

import ch.qos.logback.classic.pattern.ThrowableHandlingConverter;
import ch.qos.logback.classic.spi.*;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.boolex.EvaluationException;
import ch.qos.logback.core.boolex.EventEvaluator;
import ch.qos.logback.core.status.ErrorStatus;
import com.orange.common.logging.utils.ErrorSignature;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

/**
 * {@link ThrowableHandlingConverter} that computes and prepends a signature
 * hash in the stack trace
 * <p>
 * Requires SLF4J as the logging facade API.
 * 
 * <h2>overview</h2>
 * 
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
 * 
 * <pre style="font-size: small">
 * <span style="font-weight: bold; color: #FF5555">#d39b71d7</span>&gt; my.project.core.api.stream.StreamStoreError: An error occured while loading stream 2ada5bc3cf29411fa183546b13058264/5fe770f915864668b235031b23dd9b4a
 * 	at my.project.front.business.stream.IssStreamStore.getAsStream(IssStreamStore.java:305)
 * 	at my.project.front.controller.api.pub.DataController.queryValues(DataController.java:232)
 * 	at my.project.front.controller.api.pub.DataController$$FastClassBySpringCGLIB$$a779886d.invoke(<generated>)
 * 	at org.springframework.cglib.proxy.MethodProxy.invoke(MethodProxy.java:204)
 * 	at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.invokeJoinpoint(CglibAopProxy.java:708)
 * 	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:157)
 * 	at com.ryantenney.metrics.spring.MeteredMethodInterceptor.invoke(MeteredMethodInterceptor.java:45)
 * 	... 53 common frames omitted
 * Caused by: <span style="font-weight: bold; color: #FF5555">#4547608c</span>&gt; org.springframework.web.client.ResourceAccessException: I/O error on GET request for "https://api.iss-int.isaservicefor.me/api/v1/datasources/2ada5bc3cf29411fa183546b13058264/streams/5fe770f915864668b235031b23dd9b4a/values?pagesize=1&pagenumber=1&search=metadata.device%3Dnetatmo%3ANAMain%4070%3Aee%3A50%3A12%3Aef%3Afa":Read timed out; nested exception is java.net.SocketTimeoutException: Read timed out
 * 	at org.springframework.web.client.RestTemplate.doExecute(RestTemplate.java:561)
 * 	at org.springframework.web.client.RestTemplate.execute(RestTemplate.java:521)
 * 	at my.project.iss.IssClient.getValuesAsStream(IssClient.java:262)
 * 	at my.project.front.business.stream.IssStreamStore.getAsStream(IssStreamStore.java:285)
 * 	... 91 common frames omitted
 * Caused by: <span style="font-weight: bold; color: #FF5555">#7e585656</span>&gt; java.net.SocketTimeoutException: Read timed out
 * 	at java.net.SocketInputStream.socketRead0(Native Method)
 * 	at java.net.SocketInputStream.read(SocketInputStream.java:152)
 * 	at sun.security.ssl.InputRecord.read(InputRecord.java:480)
 * 	at com.sun.proxy.$Proxy120.receiveResponseHeader(Unknown Source)
 * 	at org.springframework.web.client.RestTemplate.doExecute(RestTemplate.java:545)
 * 	... 94 common frames omitted
 * </pre>
 * 
 * <h2>logback configuration</h2>
 * 
 * <h3>using CustomThrowableConverterWithHash with any logback appender</h3>
 * 
 * <pre style="font-size: medium">
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 * &lt;configuration&gt;
 * 
 *   &lt;!-- using the CustomThrowableConverterWithHash with mostly any logback appender --&gt;
 *   &lt;!-- 1: define "%sEx" as a conversion rule involving --&gt;
 *   &lt;conversionRule conversionWord="sEx" converterClass="com.orange.experts.utils.logging.logback.CustomThrowableConverterWithHash" /&gt;
 *   
 *   &lt;appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender"&gt;
 *      &lt;layout class="ch.qos.logback.classic.PatternLayout"&gt;
 *        &lt;!-- 2: use the "%sEx" rule in the layout pattern --&gt;
 *       &lt;Pattern&gt;%d{HH:mm:ss.SSS} %-5level %logger [%thread:%X{requestId:--}] - %msg%n%sEx&lt;/Pattern&gt;
 *     &lt;/layout&gt;
 *     &lt;!-- rest of your config ... --&gt;
 *   &lt;/appender&gt;
 *   
 *   &lt;!-- rest of your config ... --&gt;
 *   &lt;root level="INFO"&gt;
 *     &lt;appender-ref ref="STDOUT" /&gt;
 *   &lt;/root&gt;
 * 
 * &lt;/configuration&gt;
 * </pre>
 *
 * <h3>using CustomThrowableConverterWithHash with logstash-logback-appender</h3>
 * 
 * <pre style="font-size: medium">
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 * &lt;configuration&gt;
 * 
 *   &lt;!-- using the CustomThrowableConverterWithHash with any appender from logstash-logback-appender (even simpler!) --&gt;
 *   &lt;appender name="LOGSTASH" class="net.logstash.logback.appender.LogstashSocketAppender"&gt;
 *     &lt;throwableConverter class="CustomThrowableConverterWithHash" /&gt;
 *     &lt;!-- rest of your config ... --&gt;
 *   &lt;/appender&gt;
 *   
 *   &lt;!-- rest of your config ... --&gt;
 *   &lt;root level="INFO"&gt;
 *     &lt;appender-ref ref="LOGSTASH" /&gt;
 *   &lt;/root&gt;
 * 
 * &lt;/configuration&gt;
 * </pre>
 *
 * @author pismy
 * @see ErrorSignature
 */
public class CustomThrowableConverterWithHash extends ThrowableHandlingConverter {

	protected static final int BUILDER_CAPACITY = 2048;

	int lengthOption;
	List<EventEvaluator<ILoggingEvent>> evaluatorList = null;
	List<String> ignoredStackTraceLines = null;

	int errorCount = 0;

	@SuppressWarnings("unchecked")
	public void start() {

		String lengthStr = getFirstOption();

		if (lengthStr == null) {
			lengthOption = Integer.MAX_VALUE;
		} else {
			lengthStr = lengthStr.toLowerCase();
			if ("full".equals(lengthStr)) {
				lengthOption = Integer.MAX_VALUE;
			} else if ("short".equals(lengthStr)) {
				lengthOption = 1;
			} else {
				try {
					lengthOption = Integer.parseInt(lengthStr);
				} catch (NumberFormatException nfe) {
					addError("Could not parse [" + lengthStr + "] as an integer");
					lengthOption = Integer.MAX_VALUE;
				}
			}
		}

		final List optionList = getOptionList();

		if (optionList != null && optionList.size() > 1) {
			final int optionListSize = optionList.size();
			for (int i = 1; i < optionListSize; i++) {
				String evaluatorOrIgnoredStackTraceLine = (String) optionList.get(i);
				Context context = getContext();
				Map evaluatorMap = (Map) context.getObject(CoreConstants.EVALUATOR_MAP);
				EventEvaluator<ILoggingEvent> ee = (EventEvaluator<ILoggingEvent>) evaluatorMap.get(evaluatorOrIgnoredStackTraceLine);
				if (ee != null) {
					addEvaluator(ee);
				} else {
					addIgnoreStackTraceLine(evaluatorOrIgnoredStackTraceLine);
				}
			}
		}
		super.start();
	}

	private void addEvaluator(EventEvaluator<ILoggingEvent> ee) {
		if (evaluatorList == null) {
			evaluatorList = new ArrayList<EventEvaluator<ILoggingEvent>>();
		}
		evaluatorList.add(ee);
	}

	private void addIgnoreStackTraceLine(String ignoredStackTraceLine) {
		if (ignoredStackTraceLines == null) {
			ignoredStackTraceLines = new ArrayList<String>();
		}
		ignoredStackTraceLines.add(ignoredStackTraceLine);
	}

	public void stop() {
		evaluatorList = null;
		super.stop();
	}

	protected void extraData(StringBuilder builder, StackTraceElementProxy step) {
		// nop
	}

	public String convert(ILoggingEvent event) {

		IThrowableProxy tp = event.getThrowableProxy();
		if (tp == null) {
			return CoreConstants.EMPTY_STRING;
		}

		// an evaluator match will cause stack printing to be skipped
		if (evaluatorList != null) {
			boolean printStack = true;
			for (int i = 0; i < evaluatorList.size(); i++) {
				EventEvaluator<ILoggingEvent> ee = evaluatorList.get(i);
				try {
					if (ee.evaluate(event)) {
						printStack = false;
						break;
					}
				} catch (EvaluationException eex) {
					errorCount++;
					if (errorCount < CoreConstants.MAX_ERROR_COUNT) {
						addError("Exception thrown for evaluator named [" + ee.getName() + "]", eex);
					} else if (errorCount == CoreConstants.MAX_ERROR_COUNT) {
						ErrorStatus errorStatus = new ErrorStatus("Exception thrown for evaluator named [" + ee.getName() + "].", this, eex);
						errorStatus.add(new ErrorStatus("This was the last warning about this evaluator's errors."
								+ "We don't want the StatusManager to get flooded.", this));
						addStatus(errorStatus);
					}
				}
			}

			if (!printStack) {
				return CoreConstants.EMPTY_STRING;
			}
		}

		return throwableProxyToString(tp);
	}

	protected String throwableProxyToString(IThrowableProxy tp) {
		StringBuilder sb = new StringBuilder(BUILDER_CAPACITY);

		// custom
		Deque<String> hashes = null;
		if (tp instanceof ThrowableProxy) {
			hashes = ErrorSignature.hexHashes(((ThrowableProxy) tp).getThrowable());
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

	protected void subjoinSTEPArray(StringBuilder buf, int indent, IThrowableProxy tp) {
		StackTraceElementProxy[] stepArray = tp.getStackTraceElementProxyArray();
		int commonFrames = tp.getCommonFrames();

		boolean unrestrictedPrinting = lengthOption > stepArray.length;

		int maxIndex = (unrestrictedPrinting) ? stepArray.length : lengthOption;
		if (commonFrames > 0 && unrestrictedPrinting) {
			maxIndex -= commonFrames;
		}

		int ignoredCount = 0;
		for (int i = 0; i < maxIndex; i++) {
			StackTraceElementProxy element = stepArray[i];
			if (!isIgnoredStackTraceLine(element.toString())) {
				ThrowableProxyUtil.indent(buf, indent);
				printStackLine(buf, ignoredCount, element);
				ignoredCount = 0;
				buf.append(CoreConstants.LINE_SEPARATOR);
			} else {
				++ignoredCount;
				if (maxIndex < stepArray.length) {
					++maxIndex;
				}
			}
		}
		if (ignoredCount > 0) {
			printIgnoredCount(buf, ignoredCount);
			buf.append(CoreConstants.LINE_SEPARATOR);
		}

		if (commonFrames > 0 && unrestrictedPrinting) {
			ThrowableProxyUtil.indent(buf, indent);
			buf.append("... ").append(tp.getCommonFrames()).append(" common frames omitted").append(CoreConstants.LINE_SEPARATOR);
		}
	}

	private void printStackLine(StringBuilder buf, int ignoredCount, StackTraceElementProxy element) {
		buf.append(element);
		extraData(buf, element); // allow other data to be added
		if (ignoredCount > 0) {
			printIgnoredCount(buf, ignoredCount);
		}
	}

	private void printIgnoredCount(StringBuilder buf, int ignoredCount) {
		buf.append(" [").append(ignoredCount).append(" skipped]");
	}

	private boolean isIgnoredStackTraceLine(String line) {
		if (ignoredStackTraceLines != null) {
			for (String ignoredStackTraceLine : ignoredStackTraceLines) {
				if (line.contains(ignoredStackTraceLine)) {
					return true;
				}
			}
		}
		return false;
	}
}