package com.expedia.www.haystack.external;

import com.expedia.open.tracing.Span;
import com.netflix.servo.publish.AsyncMetricObserver;
import com.netflix.servo.publish.BasicMetricFilter;
import com.netflix.servo.publish.CounterToRateMetricTransform;
import com.netflix.servo.publish.MetricObserver;
import com.netflix.servo.publish.MetricPoller;
import com.netflix.servo.publish.MonitorRegistryMetricPoller;
import com.netflix.servo.publish.PollRunnable;
import com.netflix.servo.publish.PollScheduler;
import com.netflix.servo.publish.graphite.GraphiteMetricObserver;
import org.apache.commons.text.StrSubstitutor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.processor.WallclockTimestampExtractor;
import org.cfg4j.provider.ConfigurationProvider;
import org.cfg4j.provider.ConfigurationProviderBuilder;
import org.cfg4j.source.classpath.ClasspathConfigurationSource;
import org.cfg4j.source.context.filesprovider.ConfigFilesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class ProtobufToJsonTransformer {

    static final String CLIENT_ID = "External";
    static final String STARTED_MSG = "Now started ScanStream";

    static Factory factory = new Factory(); // will be mocked out in unit tests
    static Logger logger = LoggerFactory.getLogger(ProtobufToJsonTransformer.class);
    // TODO Add EnvironmentVariablesConfigurationSource object to handle env variables from apply-compose.sh et al
    private static ConfigFilesProvider cfp = () -> Collections.singletonList(Paths.get("base.yaml"));
    private static ClasspathConfigurationSource ccs = new ClasspathConfigurationSource(cfp);
    private static ConfigurationProvider cp = new ConfigurationProviderBuilder().withConfigurationSource(ccs).build();
    private static final KafkaConfig kafkaConfig = cp.bind("haystack.kafka", KafkaConfig.class);
    private static final GraphiteConfig graphiteConfig = cp.bind("haystack.graphite", GraphiteConfig.class);

    // TODO Move topics to a centralized location to be used by all services
    static final String KAFKA_FROM_TOPIC = "SpanObject-ProtobufFormat-Topic-1";
    static final String KAFKA_TO_TOPIC = "SpanObject-JsonFormat-Topic-3";

    static final String KLASS_NAME = ProtobufToJsonTransformer.class.getName();
    static final String KLASS_SIMPLE_NAME = ProtobufToJsonTransformer.class.getSimpleName();

    public static void main(String[] args) {
        initMetricsPublishing();
        final SpanProtobufDeserializer protobufDeserializer = new SpanProtobufDeserializer();
        final SpanJsonSerializer spanJsonSerializer = new SpanJsonSerializer();
        final Serde<Span> spanSerde = Serdes.serdeFrom(spanJsonSerializer, protobufDeserializer);
        final Serde<String> stringSerde = Serdes.String();

        final KStreamBuilder kStreamBuilder = factory.createKStreamBuilder();
        final KStream<String, Span> stream = kStreamBuilder.stream(stringSerde, spanSerde, KAFKA_FROM_TOPIC);
        stream.mapValues(span -> Span.newBuilder(span).build()).to(stringSerde, spanSerde, KAFKA_TO_TOPIC);

        final StreamsConfig streamsConfig = new StreamsConfig(getProperties());
        final KafkaStreams kafkaStreams = factory.createKafkaStreams(kStreamBuilder, streamsConfig);
        kafkaStreams.start();
        logger.info(STARTED_MSG);
    }

    private static void initMetricsPublishing() {
        final List<MetricObserver> observers = Collections.singletonList(createGraphiteObserver());
        PollScheduler.getInstance().start();
        schedule(new MonitorRegistryMetricPoller(), observers);
    }

    private static void schedule(MetricPoller poller, List<MetricObserver> observers) {
        final PollRunnable task = new PollRunnable(poller, BasicMetricFilter.MATCH_ALL,
                true, observers);
        PollScheduler.getInstance().addPoller(task, graphiteConfig.pollIntervalSeconds(), TimeUnit.SECONDS);
    }

    private static MetricObserver createGraphiteObserver() {
        final String rawAddress = graphiteConfig.address() + ":" + graphiteConfig.port();
        final String address = StrSubstitutor.replaceSystemProperties(rawAddress);
        return rateTransform(async(new GraphiteMetricObserver(graphiteConfig.prefix(), address)));
    }

    private static MetricObserver rateTransform(MetricObserver observer) {
        final long heartbeat = 2 * graphiteConfig.pollIntervalSeconds();
        return new CounterToRateMetricTransform(observer, heartbeat, TimeUnit.SECONDS);
    }

    private static MetricObserver async(MetricObserver observer) {
        final long expireTime = 2000 * graphiteConfig.pollIntervalSeconds();
        final int queueSize = 10;
        return new AsyncMetricObserver("graphite", observer, queueSize, expireTime);
    }

    static Properties getProperties() {
        final Properties props = new Properties();
        props.put(StreamsConfig.CLIENT_ID_CONFIG, CLIENT_ID);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, KLASS_NAME);
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, KLASS_SIMPLE_NAME);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, getKafkaIpAnPort());
        props.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, getReplicationFactor());
        props.put(StreamsConfig.TIMESTAMP_EXTRACTOR_CLASS_CONFIG, WallclockTimestampExtractor.class);
        return props;
    }

    private static String getKafkaIpAnPort() {
        return StrSubstitutor.replaceSystemProperties(kafkaConfig.brokers()) + ":" + kafkaConfig.port();
    }

    private static int getReplicationFactor() {
        final IntermediateStreamsConfig intermediateStreamsConfig = cp.bind("haystack.pipe.streams",
                IntermediateStreamsConfig.class);
        return intermediateStreamsConfig.replicationFactor();
    }

    static class Factory {
        KStreamBuilder createKStreamBuilder() {
            return new KStreamBuilder();
        }

        KafkaStreams createKafkaStreams(KStreamBuilder kStreamBuilder, StreamsConfig streamsConfig) {
            return new KafkaStreams(kStreamBuilder, streamsConfig);
        }
    }
}
