package com.catchaybk.streams.config;

import org.apache.kafka.streams.StreamsConfig;

import java.util.Properties;

public class KafkaStreamsConfigUtil {
    public static final String OPTIMIZE = "optimize";

    public static Properties getConfig() {
        Properties props = new Properties();
        props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, 3);
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 10 * 1024 * 1024L);
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);
        props.put(StreamsConfig.TOPOLOGY_OPTIMIZATION_CONFIG, OPTIMIZE);
        return props;
    }
}
