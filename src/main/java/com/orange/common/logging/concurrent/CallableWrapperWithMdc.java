package com.orange.common.logging.concurrent;

import java.util.Map;
import java.util.concurrent.Callable;

import org.slf4j.MDC;

/**
 * Helper {@link Callable} wrapper that transfers {@link MDC} context values from
 * the origin thread to the execution thread
 * 
 * @author crhx7117
 *
 */
public class CallableWrapperWithMdc<T> implements Callable<T> {
	private final Callable<T> wrapped;
	private final Map<String, String> map;

	public CallableWrapperWithMdc(Callable<T> wrapped) {
		this.wrapped = wrapped;
		// we are in the origin thread: capture the MDC
		map = MDC.getCopyOfContextMap();
	}
	
	@Override
	public T call() throws Exception {
		// we are in the execution thread: set the original MDC
		MDC.setContextMap(map);
		return wrapped.call();
	}
}