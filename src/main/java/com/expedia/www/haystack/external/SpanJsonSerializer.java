package com.expedia.www.haystack.external;

import com.expedia.open.tracing.Span;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.JsonFormat.Printer;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.charset.Charset;
import java.util.Map;

public class SpanJsonSerializer implements Serializer<Span> {

    final Printer printer = JsonFormat.printer().omittingInsignificantWhitespace();

    @Override
    public void configure(Map<String, ?> map, boolean b) {
        // Nothing to do
    }

    @Override
    public byte[] serialize(String s, Span span) {
        try {
            return printer.print(span).getBytes(Charset.forName("UTF-8"));
            // TODO metrics (count, maybe latency, maybe message size)
        } catch (Exception e) {
            // TODO Log error
        }
        return null;
    }

    @Override
    public void close() {
        // Nothing to do
    }

}
