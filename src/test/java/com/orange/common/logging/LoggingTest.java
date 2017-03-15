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
			throw new RuntimeException("test error");
		} catch(Exception e) {
			logger.error("error logging with stack", e);
		}
	}
}
