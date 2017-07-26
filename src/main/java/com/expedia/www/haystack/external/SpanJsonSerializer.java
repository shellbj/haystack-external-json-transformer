package com.expedia.www.haystack.external;

import com.expedia.open.tracing.Span;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.JsonFormat.Printer;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.Map;

public class SpanJsonSerializer implements Serializer<Span> {
    static final String ERROR_MSG = "Problem serializing span [%s]";
    static Printer printer = JsonFormat.printer().omittingInsignificantWhitespace();
    static Logger logger = LoggerFactory.getLogger(SpanJsonSerializer.class);

    @Override
    public void configure(Map<String, ?> map, boolean b) {
        // Nothing to do
    }

    @Override
    public byte[] serialize(String key, Span span) {
        try {
            return printer.print(span).getBytes(Charset.forName("UTF-8"));
            // TODO metrics (count, maybe latency, maybe message size)
        } catch (Exception exception) {
            logger.error(ERROR_MSG, span, exception);
        }
        return null;
    }

    @Override
    public void close() {
        // Nothing to do
    }

}
