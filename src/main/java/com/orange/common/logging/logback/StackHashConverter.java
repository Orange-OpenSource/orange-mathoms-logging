/*
 * Copyright (C) 2017 Orange
 *
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE.txt' in this package distribution
 * or at 'http://www.apache.org/licenses/LICENSE-2.0'.
 */
package com.orange.common.logging.logback;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.CoreConstants;
import com.orange.common.logging.utils.StackHasher;

/**
 * A Logback {@link ch.qos.logback.core.pattern.Converter} able to generate a {@code stack_hash} for a log with a stack trace
 * <p>
 * This hash is computed using {@link StackHasher}
 * 
 * @author pismy
 */
public class StackHashConverter extends ClassicConverter {

    String defaultValue;

    @Override
    public void start() {
        defaultValue = getFirstOption();
        if(defaultValue == null) {
            defaultValue = CoreConstants.EMPTY_STRING;
        }
        super.start();
    }

    @Override
    public String convert(ILoggingEvent event) {
        IThrowableProxy throwableProxy = event.getThrowableProxy();
        if (throwableProxy == null) {
            return defaultValue;
        }
        return StackHasher.hexHash(((ThrowableProxy)event.getThrowableProxy()).getThrowable());
    }
}
