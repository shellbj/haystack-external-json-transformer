package com.expedia.www.haystack.external;

import com.expedia.open.tracing.Span;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.xml.bind.DatatypeConverter;

import java.util.Map;

public class SpanProtobufDeserializer implements Deserializer<Span> {

    private Logger logger = LoggerFactory.getLogger(SpanProtobufDeserializer.class);

    @Override
    public void configure(Map<String, ?> map, boolean b) {
        // Nothing to do
    }

    @Override
    public Span deserialize(String key, byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        try {
            // TODO metrics (count, maybe latency, maybe message size)
            return Span.parseFrom(bytes);
        } catch (Exception exception) {
            logger.error("Problem deserializing span [%s]", DatatypeConverter.printHexBinary(bytes), exception);
        }
        return null;
    }

    @Override
    public void close() {
        // Nothing to do
    }
}
