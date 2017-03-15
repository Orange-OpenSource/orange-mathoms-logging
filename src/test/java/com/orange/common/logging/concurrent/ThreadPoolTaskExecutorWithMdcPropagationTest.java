package com.orange.common.logging.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.MDC;

public class ThreadPoolTaskExecutorWithMdcPropagationTest {
	@Test
	public void mdc_context_should_be_propagated() throws InterruptedException, ExecutionException {
//		ExecutorService executorService = Executors.newFixedThreadPool(4);
		ExecutorService executorService = new ThreadPoolTaskExecutorWithMdcPropagation(4, 4, 50, TimeUnit.MILLISECONDS, new LinkedBlockingQueue());
		List<Future<String>> futures = new ArrayList<Future<String>>();
		List<String> expectedTasks = new ArrayList<>();
		for(int i=0; i<100; i++) {
			MDC.put("requestId", "task"+i);
			expectedTasks.add("task"+i);
			futures.add(executorService.submit(new Callable<String>() {
				@Override
				public String call() throws Exception {
					Thread.sleep(100);
					return MDC.get("requestId");
				}
			}));
		}
		
		for(Future<String> f : futures) {
			String t = f.get();
			if(!expectedTasks.remove(t)) {
				Assert.fail("Unexpected task: "+t);
			}
		}
		if(!expectedTasks.isEmpty()) {
			Assert.fail("Expected tasks not returned: "+expectedTasks);
		}
	}
}
