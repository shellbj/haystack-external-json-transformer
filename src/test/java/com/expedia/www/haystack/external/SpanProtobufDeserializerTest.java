package com.expedia.www.haystack.external;

import com.expedia.open.tracing.Span;
import com.google.protobuf.Descriptors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.expedia.www.haystack.external.TestConstantsAndCommonCode.SERIALIZED_SPAN_BYTES;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class SpanProtobufDeserializerTest {
    private SpanProtobufDeserializer spanProtobufDeserializer;

    @Before
    public void setUp() {
        spanProtobufDeserializer = new SpanProtobufDeserializer();
    }

    @Test
    public void testDeserializeFullyPopulated() throws Descriptors.DescriptorValidationException {
        final Span actual = spanProtobufDeserializer.deserialize(null, SERIALIZED_SPAN_BYTES);

        final Span expected = TestConstantsAndCommonCode.createFullyPopulatedSpan();
        assertEquals(expected, actual);
    }
}
