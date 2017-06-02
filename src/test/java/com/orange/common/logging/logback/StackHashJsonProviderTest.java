package com.orange.common.logging.logback;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import com.fasterxml.jackson.core.JsonGenerator;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.IOException;
import java.util.regex.Pattern;

public class StackHashJsonProviderTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    private StackHashJsonProvider provider = new StackHashJsonProvider();

    @Mock
    private JsonGenerator generator;

    @Mock
    private ILoggingEvent event;

    @Mock
    private ThrowableProxy throwableProxy;

    private static Pattern HEX_PATTERN = Pattern.compile("[0-9a-fA-F]{1,8}");

    @Test
    public void testDefaultName() throws IOException {
        // GIVEN
        when(event.getThrowableProxy()).thenReturn(throwableProxy);
        when(throwableProxy.getThrowable()).thenReturn(new Exception("test error"));
        // WHEN
        provider.writeTo(generator, event);
        // THEN
        ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
        verify(generator).writeStringField(eq(StackHashJsonProvider.FIELD_NAME), hashCaptor.capture());
        Assert.assertTrue("Did not produce an hexadecimal integer: " + hashCaptor.getValue(), HEX_PATTERN.matcher(hashCaptor.getValue()).matches());
    }

    @Test
    public void testFieldName() throws IOException {
        // GIVEN
        when(event.getThrowableProxy()).thenReturn(throwableProxy);
        when(throwableProxy.getThrowable()).thenReturn(new Exception("test error"));
        provider.setFieldName("newFieldName");
        // WHEN
        provider.writeTo(generator, event);
        // THEN
        ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
        verify(generator).writeStringField(eq("newFieldName"), hashCaptor.capture());
        Assert.assertTrue("Did not produce an hexadecimal integer: " + hashCaptor.getValue(), HEX_PATTERN.matcher(hashCaptor.getValue()).matches());
    }
}
