package com.expedia.www.haystack.external;

public interface Constants {
    String SUBSYSTEM = "pipes";

    // TODO Move topics to a centralized location to be used by all services
    String KAFKA_FROM_TOPIC = "SpanObject-ProtobufFormat-Topic-1";
    String KAFKA_TO_TOPIC = "SpanObject-JsonFormat-Topic-3";
}
