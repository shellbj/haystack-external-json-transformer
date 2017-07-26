package com.expedia.www.haystack.external;

import com.expedia.open.tracing.Span;
import com.google.protobuf.Descriptors;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat.Printer;
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

    private Span span = null;
    private SpanJsonSerializer spanJsonSerializer;

    @Before
    public void setUp() throws Descriptors.DescriptorValidationException {
        span = TestConstantsAndCommonCode.createFullyPopulatedSpan();
        spanJsonSerializer = new SpanJsonSerializer();
    }

    @Test
    public void testSerializeFullyPopulated() throws Descriptors.DescriptorValidationException {
        final byte[] byteArray = spanJsonSerializer.serialize(null, span);

        final String string = new String(byteArray);
        assertEquals(TestConstantsAndCommonCode.FULLY_POPULATED_STRING, string);
    }

    @Test
    public void testSerializeNoLogsNorTagsCase() throws Descriptors.DescriptorValidationException {
        span = createSpanWithoutLogsOrTags();

        final byte[] byteArray = spanJsonSerializer.serialize(null, span);

        final String string = new String(byteArray);
        assertEquals(TestConstantsAndCommonCode.NO_TAGS_OR_LOGS_STRING, string);
    }

    private static class PrinterAndLogger {
        final Printer printer;
        final Logger logger;
        PrinterAndLogger(Printer printer, Logger logger) {
            this.printer = printer;
            this.logger = logger;
        }
    }

    @Test
    public void testSerializeExceptionCase() throws InvalidProtocolBufferException {
        final InvalidProtocolBufferException exception = new InvalidProtocolBufferException("Test");
        when(mockPrinter.print(span)).thenThrow(exception);

        final PrinterAndLogger printerAndLogger = injectMocks();
        final byte[] shouldBeNull = spanJsonSerializer.serialize(null, span);
        restoreStatics(printerAndLogger);

        assertNull(shouldBeNull);
        verify(mockPrinter).print(span);
        verify(mockLogger).error(SpanJsonSerializer.ERROR_MSG, span, exception);
        verifyNoMoreInteractions(mockPrinter, mockLogger);
    }

    @Test
    public void testConfigure() throws InvalidProtocolBufferException {
        final PrinterAndLogger printerAndLogger = injectMocks();
        spanJsonSerializer.configure(null, true);
        restoreStatics(printerAndLogger);

        verifyNoMoreInteractions(mockPrinter, mockLogger);
    }

    @Test
    public void testClose() throws InvalidProtocolBufferException {
        final PrinterAndLogger printerAndLogger = injectMocks();
        spanJsonSerializer.close();
        restoreStatics(printerAndLogger);

        verifyNoMoreInteractions(mockPrinter, mockLogger);
    }

    private PrinterAndLogger injectMocks() {
        final Printer printer = SpanJsonSerializer.printer;
        final Logger logger = SpanJsonSerializer.logger;
        SpanJsonSerializer.printer = mockPrinter;
        SpanJsonSerializer.logger = mockLogger;
        return new PrinterAndLogger(printer, logger);
    }

    private void restoreStatics(PrinterAndLogger printerAndLogger) {
        SpanJsonSerializer.printer = printerAndLogger.printer;
        SpanJsonSerializer.logger = printerAndLogger.logger;
    }

    private Span createSpanWithoutLogsOrTags() throws Descriptors.DescriptorValidationException {
        final Span.Builder spanBuilder = Span.newBuilder();
        TestConstantsAndCommonCode.addRequiredFields(spanBuilder);
        return spanBuilder.build();
    }

}
