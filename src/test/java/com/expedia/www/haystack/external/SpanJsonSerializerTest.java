package com.expedia.www.haystack.external;

import com.expedia.open.tracing.Log;
import com.expedia.open.tracing.Span;
import com.expedia.open.tracing.Tag;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import org.junit.Before;
import org.junit.Test;

import java.util.Base64;

import static org.junit.Assert.assertEquals;

public class SpanJsonSerializerTest {
    private final static String TRACE_ID = "unique-trace-id";
    private final static String SPAN_ID = "unique-span-id";
    private final static String PARENT_SPAN_ID = "unique-parent-span-id";
    private final static String OPERATION_NAME = "operation-name";
    private final static long START_TIME = 123456789;
    private final static long DURATION = 234;

    private final static long LOG0_TIMESTAMP = 234567890;
    private final static String LOG0_FIELD_KEY0 = "strField";
    private final static String LOG0_FIELD_VALUE0 = "logFieldValue";
    private final static String LOG0_FIELD_KEY1 = "longField";
    private final static long LOG0_FIELD_VALUE1 = 4567890;

    private final static long LOG1_TIMESTAMP = 234567891;
    private final static String LOG1_FIELD_KEY0 = "doubleField";
    private final static double LOG1_FIELD_VALUE0 = 6.54321;
    private final static String LOG1_FIELD_KEY1 = "boolField";
    private final static boolean LOG1_FIELD_VALUE1 = false;

    private final static String TAG_KEY0 = "strKey";
    private final static String TAG_VALUE0 = "tagValue";
    private final static String TAG_KEY1 = "longKey";
    private final static long TAG_VALUE1 = 987654321;
    private final static String TAG_KEY2 = "doubleKey";
    private final static double TAG_VALUE2 = 9876.54321;
    private final static String TAG_KEY3 = "boolField";
    private final static boolean TAG_VALUE3 = true;
    private final static String TAG_KEY4 = "boolField";
    private final static byte[] TAG_VALUE4 = {0x00, 0x01, 0x02, (byte) 0xFD, (byte) 0xFE, (byte) 0xFF};

    private final static String LOGS_FORMAT_STRING = "[" +
            "{\"timestamp\":%d,\"fields\":[{\"key\":\"%s\",\"vStr\":\"%s\"},{\"key\":\"%s\",\"vLong\":%d}]}," +
            "{\"timestamp\":%d,\"fields\":[{\"key\":\"%s\",\"vDouble\":%f},{\"key\":\"%s\",\"vBool\":%b}]}]";
    private final static String LOGS = String.format(LOGS_FORMAT_STRING,
            LOG0_TIMESTAMP, LOG0_FIELD_KEY0, LOG0_FIELD_VALUE0, LOG0_FIELD_KEY1, LOG0_FIELD_VALUE1,
            LOG1_TIMESTAMP, LOG1_FIELD_KEY0, LOG1_FIELD_VALUE0, LOG1_FIELD_KEY1, LOG1_FIELD_VALUE1);
    private final static String TAGS_FORMAT_STRING = "[" +
            "{\"key\":\"%s\",\"vStr\":\"%s\"}," +
            "{\"key\":\"%s\",\"vLong\":%d}," +
            "{\"key\":\"%s\",\"vDouble\":%f}," +
            "{\"key\":\"%s\",\"vBool\":%b}," +
            "{\"key\":\"%s\",\"vBytes\":\"%s\"}]";
    private final static String TAGS = String.format(TAGS_FORMAT_STRING,
            TAG_KEY0, TAG_VALUE0, TAG_KEY1, TAG_VALUE1, TAG_KEY2, TAG_VALUE2, TAG_KEY3, TAG_VALUE3,
            TAG_KEY4, new String(Base64.getEncoder().encode(TAG_VALUE4)));
    private final static String FULLY_POPULATED_FORMAT_STRING = "{\"traceId\":\"%s\",\"spanId\":\"%s\"," +
            "\"parentSpanId\":\"%s\",\"operationName\":\"%s\",\"startTime\":%d,\"duration\":%d," +
            "\"logs\":%s,\"tags\":%s}";
    private final static String FULLY_POPULATED_STRING = String.format(FULLY_POPULATED_FORMAT_STRING,
            TRACE_ID, SPAN_ID, PARENT_SPAN_ID, OPERATION_NAME, START_TIME, DURATION, LOGS, TAGS);
    private final static String NO_TAGS_OR_LOGS_STRING = String.format(FULLY_POPULATED_FORMAT_STRING,
            TRACE_ID, SPAN_ID, PARENT_SPAN_ID, OPERATION_NAME, START_TIME, DURATION, "[]", "[]");

    private SpanJsonSerializer spanJsonSerializer;

    @Before
    public void setUp() {
        spanJsonSerializer = new SpanJsonSerializer();
    }

    @Test
    public void testSerializeFullyPopulated() throws Descriptors.DescriptorValidationException {
        final Span span = createFullyPopulatedSpan();
        final byte[] byteArray = spanJsonSerializer.serialize(null, span);
        final String string = new String(byteArray);
        assertEquals(FULLY_POPULATED_STRING, string);
    }

    @Test
    public void testSerializeNoLogsNorTagsCase() throws Descriptors.DescriptorValidationException {
        final Span span = createSpanWithoutLogsOrTags();
        final byte[] byteArray = spanJsonSerializer.serialize(null, span);
        final String string = new String(byteArray);
        assertEquals(NO_TAGS_OR_LOGS_STRING, string);
    }

    private Span createFullyPopulatedSpan() throws Descriptors.DescriptorValidationException {
        final Span.Builder spanBuilder = Span.newBuilder();
        addRequiredFields(spanBuilder);
        spanBuilder.addLogs(createLog0());
        spanBuilder.addLogs(createLog1());
        spanBuilder.addTags(createTag0());
        spanBuilder.addTags(createTag1());
        spanBuilder.addTags(createTag2());
        spanBuilder.addTags(createTag3());
        spanBuilder.addTags(createTag4());
        return spanBuilder.build();
    }

    private Span createSpanWithoutLogsOrTags() throws Descriptors.DescriptorValidationException {
        final Span.Builder spanBuilder = Span.newBuilder();
        addRequiredFields(spanBuilder);
        return spanBuilder.build();
    }

    private void addRequiredFields(Span.Builder spanBuilder) {
        spanBuilder.setTraceId(TRACE_ID);
        spanBuilder.setSpanId(SPAN_ID);
        spanBuilder.setParentSpanId(PARENT_SPAN_ID);
        spanBuilder.setOperationName(OPERATION_NAME);
        spanBuilder.setStartTime(START_TIME);
        spanBuilder.setDuration(DURATION);
    }

    private Log createLog0() throws Descriptors.DescriptorValidationException {
        final Log.Builder logBuilder = Log.newBuilder();
        logBuilder.setTimestamp(LOG0_TIMESTAMP);
        logBuilder.addFields(createLog0Field0());
        logBuilder.addFields(createLog0Field1());
        return logBuilder.build();
    }

    private Tag createLog0Field0() throws Descriptors.DescriptorValidationException {
        final Tag.Builder fieldBuilder = Tag.newBuilder();
        fieldBuilder.setKey(LOG0_FIELD_KEY0);
        fieldBuilder.setVStr(LOG0_FIELD_VALUE0);
        return fieldBuilder.build();
    }

    private Tag createLog0Field1() throws Descriptors.DescriptorValidationException {
        final Tag.Builder fieldBuilder = Tag.newBuilder();
        fieldBuilder.setKey(LOG0_FIELD_KEY1);
        fieldBuilder.setVLong(LOG0_FIELD_VALUE1);
        return fieldBuilder.build();
    }

    private Log createLog1() throws Descriptors.DescriptorValidationException {
        final Log.Builder logBuilder = Log.newBuilder();
        logBuilder.setTimestamp(LOG1_TIMESTAMP);
        logBuilder.addFields(createLog1Field0());
        logBuilder.addFields(createLog1Field1());
        return logBuilder.build();
    }

    private Tag createLog1Field0() throws Descriptors.DescriptorValidationException {
        final Tag.Builder fieldBuilder = Tag.newBuilder();
        fieldBuilder.setKey(LOG1_FIELD_KEY0);
        fieldBuilder.setVDouble(LOG1_FIELD_VALUE0);
        return fieldBuilder.build();
    }

    private Tag createLog1Field1() throws Descriptors.DescriptorValidationException {
        final Tag.Builder fieldBuilder = Tag.newBuilder();
        fieldBuilder.setKey(LOG1_FIELD_KEY1);
        fieldBuilder.setVBool(LOG1_FIELD_VALUE1);
        return fieldBuilder.build();
    }

    private Tag createTag0() throws Descriptors.DescriptorValidationException {
        final Tag.Builder fieldBuilder = Tag.newBuilder();
        fieldBuilder.setKey(TAG_KEY0);
        fieldBuilder.setVStr(TAG_VALUE0);
        return fieldBuilder.build();
    }

    private Tag createTag1() throws Descriptors.DescriptorValidationException {
        final Tag.Builder fieldBuilder = Tag.newBuilder();
        fieldBuilder.setKey(TAG_KEY1);
        fieldBuilder.setVLong(TAG_VALUE1);
        return fieldBuilder.build();
    }

    private Tag createTag2() throws Descriptors.DescriptorValidationException {
        final Tag.Builder fieldBuilder = Tag.newBuilder();
        fieldBuilder.setKey(TAG_KEY2);
        fieldBuilder.setVDouble(TAG_VALUE2);
        return fieldBuilder.build();
    }

    private Tag createTag3() throws Descriptors.DescriptorValidationException {
        final Tag.Builder fieldBuilder = Tag.newBuilder();
        fieldBuilder.setKey(TAG_KEY3);
        fieldBuilder.setVBool(TAG_VALUE3);
        return fieldBuilder.build();
    }

    private Tag createTag4() throws Descriptors.DescriptorValidationException {
        final Tag.Builder fieldBuilder = Tag.newBuilder();
        fieldBuilder.setKey(TAG_KEY4);
        fieldBuilder.setVBytes(ByteString.copyFrom(TAG_VALUE4));
        return fieldBuilder.build();
    }
}
