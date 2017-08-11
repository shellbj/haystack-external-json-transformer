package com.expedia.www.haystack.external;

import com.expedia.open.tracing.Span;
import com.google.protobuf.Descriptors;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat.Printer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SpanJsonSerializerTest {

    @Mock
    private Printer mockPrinter;
    @Mock
    private Logger mockLogger;
    private Logger realLogger;

    private Span span = null;
    private SpanJsonSerializer spanJsonSerializer;

    @Before
    public void setUp() throws InvalidProtocolBufferException {
        span = TestConstantsAndCommonCode.createFullyPopulatedSpan();
        spanJsonSerializer = new SpanJsonSerializer();
        realLogger = SpanJsonSerializer.logger;
        SpanJsonSerializer.logger = mockLogger;
    }

    @After
    public void tearDown() {
        SpanJsonSerializer.logger = realLogger;
        SpanJsonSerializer.errorCount.increment(-((long) SpanJsonSerializer.errorCount.getValue()));
        SpanJsonSerializer.requestCount.increment(-((long) SpanJsonSerializer.requestCount.getValue()));
        SpanJsonSerializer.bytesIn.increment(-((long) SpanJsonSerializer.bytesIn.getValue()));
        verifyNoMoreInteractions(mockPrinter, mockLogger);
    }

    @Test
    public void testSerializeFullyPopulated() throws Descriptors.DescriptorValidationException {
        final byte[] byteArray = spanJsonSerializer.serialize(null, span);

        final String string = new String(byteArray);
        assertEquals(TestConstantsAndCommonCode.JSON_SPAN_STRING, string);
        verifyCounters(0L, 1, byteArray.length);
    }

    @Test
    public void testSerializeExceptionCase() throws InvalidProtocolBufferException {
        final InvalidProtocolBufferException exception = new InvalidProtocolBufferException("Test");
        when(mockPrinter.print(span)).thenThrow(exception);

        final Printer printer = injectMockPrinter();
        final byte[] shouldBeNull = spanJsonSerializer.serialize(null, span);
        SpanJsonSerializer.printer = printer;

        assertNull(shouldBeNull);
        verify(mockPrinter).print(span);
        verify(mockLogger).error(SpanJsonSerializer.ERROR_MSG, span, exception);
        verifyCounters(1, 1, 0);
    }

    @Test
    public void testConfigure() throws InvalidProtocolBufferException {
        final Printer printer = injectMockPrinter();
        spanJsonSerializer.configure(null, true);
        SpanJsonSerializer.printer = printer;
        verifyCounters(0, 0, 0);
    }

    @Test
    public void testClose() throws InvalidProtocolBufferException {
        final Printer printer = injectMockPrinter();
        spanJsonSerializer.close();
        SpanJsonSerializer.printer = printer;
        verifyCounters(0, 0, 0);
    }

    private Printer injectMockPrinter() {
        final Printer printer = SpanJsonSerializer.printer;
        SpanJsonSerializer.printer = mockPrinter;
        return printer;
    }

    private void verifyCounters(long errorCount, long requestCount, long bytesIn) {
        assertEquals(errorCount, SpanJsonSerializer.errorCount.getValue());
        assertEquals(requestCount, SpanJsonSerializer.requestCount.getValue());
        assertEquals(bytesIn, SpanJsonSerializer.bytesIn.getValue());
    }
}
