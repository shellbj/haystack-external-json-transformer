package com.expedia.www.haystack.external;

import com.expedia.open.tracing.Span;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.processor.WallclockTimestampExtractor;

import java.util.Properties;

// TODO Write a test to exercise this code (probably an integration test)
public class ProtobufToJsonTransformer {

    private final static String CLIENT_ID = "External";

    // TODO Move topics to a centralized location to be used by all services
    private final static String KAFKA_FROM_TOPIC = "SpanObject-ProtobufFormat-Topic-1";
    private final static String KAFKA_TO_TOPIC = "SpanObject-JsonFormat-Topic-3";

    private final static String KLASS_NAME = ProtobufToJsonTransformer.class.getName();
    private final static String KLASS_SIMPLE_NAME = ProtobufToJsonTransformer.class.getSimpleName();

    public static void main(String[] args) {
        final SpanProtobufDeserializer protobufDeserializer = new SpanProtobufDeserializer();
        final SpanJsonSerializer spanJsonSerializer = new SpanJsonSerializer();
        final Serde<Span> spanSerde = Serdes.serdeFrom(spanJsonSerializer, protobufDeserializer);
        final Serde<String> stringSerde = Serdes.String();

        final KStreamBuilder kStreamBuilder = new KStreamBuilder();
        kStreamBuilder.stream(stringSerde, spanSerde, KAFKA_FROM_TOPIC)
                .mapValues(span -> Span.newBuilder(span).build()).to(stringSerde, spanSerde, KAFKA_TO_TOPIC);

        final StreamsConfig streamsConfig = new StreamsConfig(getProperties());
        final KafkaStreams kafkaStreams = new KafkaStreams(kStreamBuilder, streamsConfig);
        kafkaStreams.start();
        System.out.println("Now started ScanStream"); // TODO Use a logger instead of System.out.println()
    }

    // TODO Read configurations from a file with environment-specific values
    private static Properties getProperties() {
        final Properties props = new Properties();
        props.put(StreamsConfig.CLIENT_ID_CONFIG, CLIENT_ID);
        props.put("group.id", KLASS_NAME);
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, KLASS_SIMPLE_NAME);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.99.101:9092");
        props.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, 1);
        props.put(StreamsConfig.TIMESTAMP_EXTRACTOR_CLASS_CONFIG, WallclockTimestampExtractor.class);
        return props;
    }
}
