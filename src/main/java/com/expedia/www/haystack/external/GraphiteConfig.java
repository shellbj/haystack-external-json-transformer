package com.expedia.www.haystack.external;

public interface GraphiteConfig {
    String prefix();

    String address();

    int port();

    int pollIntervalSeconds();
}
