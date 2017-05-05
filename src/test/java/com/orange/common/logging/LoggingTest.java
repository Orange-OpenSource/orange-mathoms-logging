package com.orange.common.logging;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketTimeoutException;

public class LoggingTest {
	@Test
	public void test_logging() {
		Logger logger = LoggerFactory.getLogger(LoggingTest.class);
		logger.info("info logging");
		try {
			new MyClient().getTheThings();
		} catch(Exception e) {
			logger.error("error logging with stack", e);
		}
	}
	private static class MyClient {
		public String getTheThings() throws MyClientException {
			try {
				return new HttpStack().get("http://dummy/things");
			} catch(HttpStack.HttpError e) {
				throw new MyClientException("An error occurred while getting the things", e);
			}
		}
		private static class MyClientException extends Exception {
			MyClientException(String message, Throwable cause) {
				super(message, cause);
			}
		}
	}
	private static class HttpStack {
		public String get(String uri) throws HttpError {
			try {
				throw new SocketTimeoutException("Read timed out");
			} catch(IOException ioe) {
				throw new HttpError("I/O error on GET request for "+uri, ioe);
			}
		}
		private static class HttpError extends Exception {
			HttpError(String message, Throwable cause) {
				super(message, cause);
			}
		}
	}
}
