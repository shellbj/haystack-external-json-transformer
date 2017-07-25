package com.expedia.www.haystack.external;

import com.expedia.open.tracing.Span;
import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class SpanProtobufDeserializer implements Deserializer<Span> {
    @Override
    public void configure(Map<String, ?> map, boolean b) {
        // Nothing to do
    }

    @Override
    public Span deserialize(String s, byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        try {
            // TODO metrics (count, maybe latency, maybe message size)
            return Span.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            // TODO Log error
        }
        return null;
    }

    @Override
    public void close() {
        // Nothing to do
    }
}
