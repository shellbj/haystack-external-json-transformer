package com.expedia.www.haystack.external;

import com.expedia.open.tracing.Span;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import static com.expedia.www.haystack.external.SpanProtobufDeserializer.ERROR_MSG;
import static com.expedia.www.haystack.external.TestConstantsAndCommonCode.PROTOBUF_SPAN_BYTES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class SpanProtobufDeserializerTest {
    @Mock
    private Logger mockLogger;
    private Logger realLogger;

    private SpanProtobufDeserializer spanProtobufDeserializer;

    @Before
    public void setUp() {
        spanProtobufDeserializer = new SpanProtobufDeserializer();
        realLogger = SpanProtobufDeserializer.logger;
        SpanProtobufDeserializer.logger = mockLogger;
    }

    @After
    public void tearDown() {
        SpanProtobufDeserializer.logger = realLogger;
        verifyNoMoreInteractions(mockLogger);
    }

    @Test
    public void testDeserializeFullyPopulated() throws InvalidProtocolBufferException {
        final Span actual = spanProtobufDeserializer.deserialize(null, PROTOBUF_SPAN_BYTES);

        final Span expected = TestConstantsAndCommonCode.createFullyPopulatedSpan();
        assertEquals(expected, actual);
    }

    @Test
    public void testDeserializeNull() {
        final Span shouldBeNull = spanProtobufDeserializer.deserialize(null, null);

        assertNull(shouldBeNull);
    }

    @Test
    public void testDeserializeExceptionCase() {
        final Span shouldBeNull = spanProtobufDeserializer.deserialize(null, new byte[]{ 0x00 });

        assertNull(shouldBeNull);
        verify(mockLogger).error(eq(ERROR_MSG), eq("00"), any(InvalidProtocolBufferException.class));
    }

    @Test
    public void testConfigure() throws InvalidProtocolBufferException {
        spanProtobufDeserializer.configure(null, true);

        verifyNoMoreInteractions(mockLogger);
    }

    @Test
    public void testClose() throws InvalidProtocolBufferException {
        spanProtobufDeserializer.close();
    }

}
