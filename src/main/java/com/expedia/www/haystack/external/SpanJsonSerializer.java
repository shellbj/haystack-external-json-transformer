package com.expedia.www.haystack.external;

import com.expedia.open.tracing.Span;
import com.expedia.www.haystack.metrics.MetricObjects;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.JsonFormat.Printer;
import com.netflix.servo.monitor.Counter;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.Map;

import static com.expedia.www.haystack.external.Constants.SUBSYSTEM;

public class SpanJsonSerializer implements Serializer<Span> {
    static final String ERROR_MSG = "Problem serializing span [%s]";
    static Printer printer = JsonFormat.printer().omittingInsignificantWhitespace();
    static Logger logger = LoggerFactory.getLogger(SpanJsonSerializer.class);
    private static final String KLASS_NAME = SpanJsonSerializer.class.getSimpleName();

    // The Servo Counter generates a RATE metric; using upper case arguments for counterName below (like REQUEST and
    // ERROR) which should match the name of the static final instance variable result in metrics like REQUEST_RATE and
    // ERROR_RATE in influxDb because of the way that HaystackGraphiteNamingConvention builds metric names.
    static final Counter REQUEST = MetricObjects.createAndRegisterCounter(SUBSYSTEM, KLASS_NAME, "REQUEST");
    static final Counter ERROR = MetricObjects.createAndRegisterCounter(SUBSYSTEM, KLASS_NAME, "ERROR");
    static final Counter BYTES_IN = MetricObjects.createAndRegisterCounter(SUBSYSTEM, KLASS_NAME, "BYTES_IN");

    @Override
    public void configure(Map<String, ?> map, boolean b) {
        // Nothing to do
    }

    @Override
    public byte[] serialize(String key, Span span) {
        try {
            REQUEST.increment();
            final byte[] bytes = printer.print(span).getBytes(Charset.forName("UTF-8"));
            BYTES_IN.increment(bytes.length);
            return bytes;
        } catch (Exception exception) {
            ERROR.increment();
            logger.error(ERROR_MSG, span, exception);
        }
        return null;
    }

    @Override
    public void close() {
        // Nothing to do
    }

}
