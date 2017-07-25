package com.expedia.www.haystack.external;

import com.expedia.open.tracing.Log;
import com.expedia.open.tracing.Span;
import com.expedia.open.tracing.Tag;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.charset.Charset;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class SpanJsonSerializer implements Serializer<Span> {

    @Override
    public void configure(Map<String, ?> map, boolean b) {
        // Nothing to do
    }

    @Override
    public byte[] serialize(String s, Span span) {
        try {
            // TODO Replace with gson-based serialization from protobuf 3 code
            // TODO metrics (count, maybe latency, maybe message size)
            return JsonSerializer.serialize(span).getBytes(Charset.forName("UTF-8"));
        } catch (Exception e) {
            // TODO Log error
        }
        return null;
    }

    @Override
    public void close() {
        // Nothing to do
    }

    /**
     * This class does the actual serialization. In Protobuf 3, JSON serialization is easy:
     * <ul><li>
     * <code>JsonFormat.printer().includingDefaultValueFields().printer.print(span).getBytes(Charset.forName("UTF-8"))</code>
     * </li></ul>
     * but this approach exhibited a problem with protobuf 3.3.1: an extra JSON attribute
     * (<code>"type": "STRING"</code>) was sometimes added to objects</li>
     * Jackson serialization would be difficult because the Span class is generated code (and can therefore not be
     * annotated with Jackson custom tags) and the names of the attributes inside the protobuf object are not the same
     * as what was specified in the .proto files.
     */
    static class JsonSerializer {
        private final static String TRACE_ID = "\"traceId\":\"%s\"";
        private final static String SPAN_ID = "\"spanId\":\"%s\"";
        private final static String PARENT_SPAN_ID = "\"parentSpanId\":\"%s\"";
        private final static String OPERATION_NAME = "\"operationName\":\"%s\"";
        private final static String START_TIME = "\"startTime\":%d";
        private final static String DURATION = "\"duration\":%d";
        private final static String LOGS = "\"logs\":[%s]";
        private final static String TAGS = "\"tags\":[%s]";
        private final static String LOG = "{\"timestamp\":%d,\"fields\":[%s]}";
        private final static String STRING_FIELD_OR_TAG = "{\"key\":\"%s\",\"vStr\":\"%s\"}";
        private final static String LONG_FIELD_OR_TAG = "{\"key\":\"%s\",\"vLong\":%d}";
        private final static String DOUBLE_FIELD_OR_TAG = "{\"key\":\"%s\",\"vDouble\":%f}";
        private final static String BOOL_FIELD_OR_TAG = "{\"key\":\"%s\",\"vBool\":%b}";
        private final static String BYTES_FIELD_OR_TAG = "{\"key\":\"%s\",\"vBytes\":\"%s\"}";
        private final static String LINE = String.format("{%s,%s,%s,%s,%s,%s,%s,%s}",
                TRACE_ID, SPAN_ID, PARENT_SPAN_ID, OPERATION_NAME, START_TIME, DURATION, LOGS, TAGS);

        static String serialize(final Span span) {
            return String.format(LINE, span.getTraceId(), span.getSpanId(), span.getParentSpanId(),
                    span.getOperationName(), span.getStartTime(), span.getDuration(), createLogs(span),
                    createTags(span));
        }

        private static String createLogs(final Span span) {
            final String[] logs = new String[span.getLogsList().size()];
            int logIndex = 0;
            for (final Log log : span.getLogsList()) {
                logs[logIndex++] = String.format(LOG, log.getTimestamp(),
                        createFieldsOrTags(log.getFieldsList()));
            }
            return String.join(",", logs);
        }

        private static String createTags(final Span span) {
            return createFieldsOrTags(span.getTagsList());
        }

        private static String createFieldsOrTags(List<Tag> fieldsOrTagsList) {
            int index = 0;
            final String[] fieldsOrTags = new String[fieldsOrTagsList.size()];
            for (final Tag field : fieldsOrTagsList) {
                final Tag.MyvalueCase tagType = field.getMyvalueCase();
                switch (tagType) {
                    case VSTR:
                        fieldsOrTags[index] = String.format(STRING_FIELD_OR_TAG, field.getKey(), field.getVStr());
                        break;
                    case VDOUBLE:
                        fieldsOrTags[index] = String.format(DOUBLE_FIELD_OR_TAG, field.getKey(), field.getVDouble());
                        break;
                    case VBOOL:
                        fieldsOrTags[index] = String.format(BOOL_FIELD_OR_TAG, field.getKey(), field.getVBool());
                        break;
                    case VLONG:
                        fieldsOrTags[index] = String.format(LONG_FIELD_OR_TAG, field.getKey(), field.getVLong());
                        break;
                    case VBYTES:
                        final byte[] bytes = Base64.getEncoder().encode(field.getVBytes().toByteArray());
                        fieldsOrTags[index] = String.format(BYTES_FIELD_OR_TAG, field.getKey(), new String(bytes));
                        break;
                }
                index++;
            }
            return String.join(",", fieldsOrTags);
        }
    }
}
