package com.sefa.sensor.ingest;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaTopicConfig {

    public static final String SENSOR_MEASUREMENTS = "sensor-measurements";

    @Bean
    NewTopic sensorMeasurementsTopic() {
        return TopicBuilder.name(SENSOR_MEASUREMENTS).partitions(3).replicas(1).build();
    }

    @Bean
    NewTopic sensorMeasurementsDLTTopic() {
        return TopicBuilder.name(SENSOR_MEASUREMENTS + "-dlt").partitions(3).replicas(1).build();
    }

    @Bean
    DefaultErrorHandler kafkaErrorHandler(ProducerFactory<Object, Object> producerFactory,
            KafkaTemplate<Object, Object> jsonTemplate) {

        KafkaTemplate<Object, Object> bytesTemplate = new KafkaTemplate<>(producerFactory,
                Map.of(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class));

        Map<Class<?>, KafkaOperations<?, ?>> templates = new LinkedHashMap<>();
        templates.put(byte[].class, bytesTemplate);
        templates.put(Object.class, jsonTemplate);

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(templates);
        return new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 2));
    }
}
