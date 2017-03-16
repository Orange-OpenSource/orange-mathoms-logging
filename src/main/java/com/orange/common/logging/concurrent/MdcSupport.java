/*
 * Copyright (C) 2017 Orange
 *
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE.txt' in this package distribution
 * or at 'http://www.apache.org/licenses/LICENSE-2.0'.
 */
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
