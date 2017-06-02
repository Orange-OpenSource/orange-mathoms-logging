/*
 * Copyright (C) 2017 Orange
 *
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE.txt' in this package distribution
 * or at 'http://www.apache.org/licenses/LICENSE-2.0'.
 */
package com.orange.common.logging.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import com.fasterxml.jackson.core.JsonGenerator;
import com.orange.common.logging.utils.StackElementFilter;
import com.orange.common.logging.utils.ThrowableHasher;
import net.logstash.logback.composite.AbstractFieldJsonProvider;
import net.logstash.logback.composite.JsonWritingUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A JSON provider that works with the <a href="https://github.com/logstash/logstash-logback-encoder">logstash-logback-encoder</a>
 * encoder, that adds a {@code error_hash} Json field on a log with a stack trace
 * <p>
 * This hash is computed using {@link ThrowableHasher}
 * 
 * @author pismy
 */
public class StackHashJsonProvider extends AbstractFieldJsonProvider<ILoggingEvent> {

    public static final String FIELD_NAME = "stack_hash";

    /**
     * Patterns used to determine which stacktrace elements to exclude from hash computation.
     *
     * The strings being matched against are in the form "fullyQualifiedClassName.methodName"
     * (e.g. "java.lang.Object.toString").
     */
    private List<Pattern> excludes = new ArrayList<Pattern>(5);

    private ThrowableHasher hasher = new ThrowableHasher();

    public StackHashJsonProvider() {
        setFieldName(FIELD_NAME);
    }

    @Override
    public void start() {
        if(!excludes.isEmpty()) {
            hasher = new ThrowableHasher(StackElementFilter.byPattern(excludes));
        }
        super.start();
    }

    public void addExclude(String exclusionPattern) {
        excludes.add(Pattern.compile(exclusionPattern));
    }

    public void setExcludes(List<String> exclusionPatterns) {
        if (exclusionPatterns == null || exclusionPatterns.isEmpty()) {
            this.excludes = new ArrayList<Pattern>(5);
        } else {
            this.excludes = new ArrayList<Pattern>(exclusionPatterns.size());
            for (String pattern : exclusionPatterns) {
                addExclude(pattern);
            }
        }
    }

    public List<String> getExcludes() {
        List<String> exclusionPatterns = new ArrayList<String>(excludes.size());
        for (Pattern pattern : excludes) {
            exclusionPatterns.add(pattern.pattern());
        }
        return exclusionPatterns;
    }

    @Override
    public void writeTo(JsonGenerator generator, ILoggingEvent event) throws IOException {
        IThrowableProxy throwableProxy = event.getThrowableProxy();
        if (throwableProxy != null && throwableProxy instanceof  ThrowableProxy) {
            String hash = hasher.hexHash(((ThrowableProxy)event.getThrowableProxy()).getThrowable());
            JsonWritingUtils.writeStringField(generator, getFieldName(), hash);
        }
    }
}
