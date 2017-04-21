package com.orange.common.logging;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingTest {
	@Test
	public void test_logging() {
		Logger logger = LoggerFactory.getLogger(LoggingTest.class);
		logger.info("info logging");
		try {
			wrappedError();
		} catch(Exception e) {
			logger.error("error logging with stack", e);
		}
	}
	private static void wrappedError() throws Exception {
		try {
			rootError();
		} catch (Exception e) {
			throw new Exception("wrapped error", e);
		}
	}
	private static void rootError() throws Exception {
		throw new Exception("root error");
	}
}
