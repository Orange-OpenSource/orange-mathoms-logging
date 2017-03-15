package com.orange.common.logging.concurrent;

import java.util.concurrent.Callable;

public class MdcSupport {
    private MdcSupport() {
    }

    public static <T> Callable<T> wrap(Callable<T> callable) {
        return new CallableWrapperWithMdc<>(callable);
    }

    public static Runnable wrap(Runnable runnable) {
        return new RunnableWrapperWithMdc(runnable);
    }
}
