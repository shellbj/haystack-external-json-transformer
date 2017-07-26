package com.expedia.www.haystack.external;

import com.expedia.open.tracing.Log;
import com.expedia.open.tracing.Span;
import com.expedia.open.tracing.Tag;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.util.Base64;

class TestConstantsAndCommonCode {
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
    private final static String LOG1_FIELD_VALUE0 = "6.54321";
    private final static String LOG1_FIELD_KEY1 = "boolField";
    private final static boolean LOG1_FIELD_VALUE1 = false;
    private final static String TAG_KEY0 = "strKey";
    private final static String TAG_VALUE0 = "tagValue";
    private final static String TAG_KEY1 = "longKey";
    private final static long TAG_VALUE1 = 987654321;
    private final static String TAG_KEY2 = "doubleKey";
    private final static double TAG_VALUE2 = 9876.54321;
    private final static String TAG_KEY3 = "boolKey";
    private final static boolean TAG_VALUE3 = true;
    private final static String TAG_KEY4 = "bytesKey";
    private final static byte[] TAG_VALUE4 = {0x00, 0x01, 0x02, (byte) 0xFD, (byte) 0xFE, (byte) 0xFF};
    private final static String LOGS_FORMAT_STRING = "\"logs\":[" +
            "{\"timestamp\":\"%d\",\"fields\":[{\"key\":\"%s\",\"vStr\":\"%s\"},{\"key\":\"%s\",\"vLong\":\"%d\"}]}," +
            "{\"timestamp\":\"%d\",\"fields\":[{\"key\":\"%s\",\"vDouble\":%s},{\"key\":\"%s\",\"vBool\":%b}]}]";
    private final static String LOGS = String.format(LOGS_FORMAT_STRING,
            LOG0_TIMESTAMP, LOG0_FIELD_KEY0, LOG0_FIELD_VALUE0, LOG0_FIELD_KEY1, LOG0_FIELD_VALUE1,
            LOG1_TIMESTAMP, LOG1_FIELD_KEY0, LOG1_FIELD_VALUE0, LOG1_FIELD_KEY1, LOG1_FIELD_VALUE1);
    private final static String TAGS_FORMAT_STRING = "\"tags\":[" +
            "{\"key\":\"%s\",\"vStr\":\"%s\"}," +
            "{\"key\":\"%s\",\"vLong\":\"%d\"}," +
            "{\"key\":\"%s\",\"vDouble\":%s}," +
            "{\"key\":\"%s\",\"vBool\":%b}," +
            "{\"key\":\"%s\",\"vBytes\":\"%s\"}]";
    private final static String TAGS = String.format(TAGS_FORMAT_STRING,
            TAG_KEY0, TAG_VALUE0, TAG_KEY1, TAG_VALUE1, TAG_KEY2, TAG_VALUE2, TAG_KEY3, TAG_VALUE3,
            TAG_KEY4, new String(Base64.getEncoder().encode(TAG_VALUE4)));
    private final static String FULLY_POPULATED_FORMAT_STRING = "{\"traceId\":\"%s\",\"spanId\":\"%s\"," +
            "\"parentSpanId\":\"%s\",\"operationName\":\"%s\",\"startTime\":\"%d\",\"duration\":\"%d\"";
    final static String NO_TAGS_OR_LOGS_STRING = String.format(FULLY_POPULATED_FORMAT_STRING,
            TRACE_ID, SPAN_ID, PARENT_SPAN_ID, OPERATION_NAME, START_TIME, DURATION) + "}";
    final static String FULLY_POPULATED_STRING = String.format(FULLY_POPULATED_FORMAT_STRING, TRACE_ID, SPAN_ID,
            PARENT_SPAN_ID, OPERATION_NAME, START_TIME, DURATION) + String.format(",%s,%s}", LOGS, TAGS);
    @SuppressWarnings("SpellCheckingInspection")
    private final static String SERIALIZED_SPAN_STRING =
            "0a0f756e697175652d74726163652d6964120e756e697175652d7370616e2d69" +
            "641a15756e697175652d706172656e742d7370616e2d6964220e6f7065726174" +
            "696f6e2d6e616d6528959aef3a30ea013a3208d2f1ec6f12190a087374724669" +
            "656c641a0d6c6f674669656c6456616c756512100a096c6f6e674669656c6420" +
            "d2e696023a2c08d3f1ec6f12160a0b646f75626c654669656c6429ce70033e3f" +
            "2c1a40120d0a09626f6f6c4669656c64300042120a067374724b65791a087461" +
            "6756616c7565420f0a076c6f6e674b657920b1d1f9d60342140a09646f75626c" +
            "654b6579296ec0e787454ac340420b0a07626f6f6c4b6579300142120a086279" +
            "7465734b65793a06000102fdfeff";
    final static byte[] SERIALIZED_SPAN_BYTES = (new HexBinaryAdapter()).unmarshal(SERIALIZED_SPAN_STRING);

    static void addRequiredFields(Span.Builder spanBuilder) {
        spanBuilder.setTraceId(TRACE_ID);
        spanBuilder.setSpanId(SPAN_ID);
        spanBuilder.setParentSpanId(PARENT_SPAN_ID);
        spanBuilder.setOperationName(OPERATION_NAME);
        spanBuilder.setStartTime(START_TIME);
        spanBuilder.setDuration(DURATION);
    }

    private static Log createLog0() throws Descriptors.DescriptorValidationException {
        final Log.Builder logBuilder = Log.newBuilder();
        logBuilder.setTimestamp(LOG0_TIMESTAMP);
        logBuilder.addFields(createLog0Field0());
        logBuilder.addFields(createLog0Field1());
        return logBuilder.build();
    }

    private static Tag createLog0Field0() throws Descriptors.DescriptorValidationException {
        final Tag.Builder fieldBuilder = Tag.newBuilder();
        fieldBuilder.setKey(LOG0_FIELD_KEY0);
        fieldBuilder.setVStr(LOG0_FIELD_VALUE0);
        return fieldBuilder.build();
    }

    private static Tag createLog0Field1() throws Descriptors.DescriptorValidationException {
        final Tag.Builder fieldBuilder = Tag.newBuilder();
        fieldBuilder.setKey(LOG0_FIELD_KEY1);
        fieldBuilder.setVLong(LOG0_FIELD_VALUE1);
        return fieldBuilder.build();
    }

    private static Log createLog1() throws Descriptors.DescriptorValidationException {
        final Log.Builder logBuilder = Log.newBuilder();
        logBuilder.setTimestamp(LOG1_TIMESTAMP);
        logBuilder.addFields(createLog1Field0());
        logBuilder.addFields(createLog1Field1());
        return logBuilder.build();
    }

    private static Tag createLog1Field0() throws Descriptors.DescriptorValidationException {
        final Tag.Builder fieldBuilder = Tag.newBuilder();
        fieldBuilder.setKey(LOG1_FIELD_KEY0);
        fieldBuilder.setVDouble(Double.parseDouble(LOG1_FIELD_VALUE0));
        return fieldBuilder.build();
    }

    private static Tag createLog1Field1() throws Descriptors.DescriptorValidationException {
        final Tag.Builder fieldBuilder = Tag.newBuilder();
        fieldBuilder.setKey(LOG1_FIELD_KEY1);
        fieldBuilder.setVBool(LOG1_FIELD_VALUE1);
        return fieldBuilder.build();
    }

    private static Tag createTag0() throws Descriptors.DescriptorValidationException {
        final Tag.Builder fieldBuilder = Tag.newBuilder();
        fieldBuilder.setKey(TAG_KEY0);
        fieldBuilder.setVStr(TAG_VALUE0);
        return fieldBuilder.build();
    }

    private static Tag createTag1() throws Descriptors.DescriptorValidationException {
        final Tag.Builder fieldBuilder = Tag.newBuilder();
        fieldBuilder.setKey(TAG_KEY1);
        fieldBuilder.setVLong(TAG_VALUE1);
        return fieldBuilder.build();
    }

    private static Tag createTag2() throws Descriptors.DescriptorValidationException {
        final Tag.Builder fieldBuilder = Tag.newBuilder();
        fieldBuilder.setKey(TAG_KEY2);
        fieldBuilder.setVDouble(TAG_VALUE2);
        return fieldBuilder.build();
    }

    private static Tag createTag3() throws Descriptors.DescriptorValidationException {
        final Tag.Builder fieldBuilder = Tag.newBuilder();
        fieldBuilder.setKey(TAG_KEY3);
        fieldBuilder.setVBool(TAG_VALUE3);
        return fieldBuilder.build();
    }

    private static Tag createTag4() throws Descriptors.DescriptorValidationException {
        final Tag.Builder fieldBuilder = Tag.newBuilder();
        fieldBuilder.setKey(TAG_KEY4);
        fieldBuilder.setVBytes(ByteString.copyFrom(TAG_VALUE4));
        return fieldBuilder.build();
    }

    static Span createFullyPopulatedSpan() throws Descriptors.DescriptorValidationException {
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
}
