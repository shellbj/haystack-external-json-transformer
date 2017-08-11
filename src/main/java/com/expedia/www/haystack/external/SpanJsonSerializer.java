package com.expedia.www.haystack.external;

import com.expedia.open.tracing.Span;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.JsonFormat.Printer;
import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.servo.annotations.Monitor;
import com.netflix.servo.monitor.Counter;
import com.netflix.servo.monitor.Monitors;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static com.netflix.servo.annotations.DataSourceType.COUNTER;

public class SpanJsonSerializer implements Serializer<Span> {
    static final String ERROR_MSG = "Problem serializing span [%s]";
    static Printer printer = JsonFormat.printer().omittingInsignificantWhitespace();
    static Logger logger = LoggerFactory.getLogger(SpanJsonSerializer.class);

    static final Counter requestCount = Monitors.newCounter("requestCount");
    static final Counter errorCount = Monitors.newCounter("errorCount");
    static final Counter bytesIn = Monitors.newCounter("bytesIn");

    static {
        DefaultMonitorRegistry.getInstance().register(requestCount);
        DefaultMonitorRegistry.getInstance().register(errorCount);
        DefaultMonitorRegistry.getInstance().register(bytesIn);
    }
    @Override
    public void configure(Map<String, ?> map, boolean b) {
        // Nothing to do
    }

    @Override
    public byte[] serialize(String key, Span span) {
        try {
            requestCount.increment();
            final byte[] bytes = printer.print(span).getBytes(Charset.forName("UTF-8"));
            bytesIn.increment(bytes.length);
            return bytes;
        } catch (Exception exception) {
            errorCount.increment();
            logger.error(ERROR_MSG, span, exception);
        }
        return null;
    }

    @Override
    public void close() {
        // Nothing to do
    }

}
