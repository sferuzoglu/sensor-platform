package com.sefa.sensor.ingest;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String SENSOR_MEASUREMENTS = "sensor-measurements";

    @Bean
    NewTopic sensorMeasurementsTopic() {
        return TopicBuilder.name(SENSOR_MEASUREMENTS).partitions(3).replicas(1).build();
    }
}
