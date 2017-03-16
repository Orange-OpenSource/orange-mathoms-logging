/*
 * Copyright (C) 2017 Orange
 *
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE.txt' in this package distribution
 * or at 'http://www.apache.org/licenses/LICENSE-2.0'.
 */
package com.orange.common.logging.logback;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.orange.common.logging.utils.ErrorSignature;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import net.logstash.logback.composite.AbstractFieldJsonProvider;
import net.logstash.logback.composite.JsonWritingUtils;

/**
 * A JSON provider that works with the <a href="https://github.com/logstash/logstash-logback-encoder">logstash-logback-encoder</a>
 * encoder, and adds a {@code stack_hash} Json field on a log with a stack trace
 * <p>
 * This hash is computed using {@link ErrorSignature}
 * 
 * @author pismy
 */
public class StackHashJsonProvider extends AbstractFieldJsonProvider<ILoggingEvent> {

    public static final String FIELD_NAME = "stack_hash";

    public StackHashJsonProvider() {
        setFieldName(FIELD_NAME);
    }
    
    @Override
    public void writeTo(JsonGenerator generator, ILoggingEvent event) throws IOException {
        IThrowableProxy throwableProxy = event.getThrowableProxy();
        if (throwableProxy != null) {
        	String hash = ErrorSignature.hexHash(((ThrowableProxy)event.getThrowableProxy()).getThrowable());
            JsonWritingUtils.writeStringField(generator, getFieldName(), hash);
        }
    }
}
