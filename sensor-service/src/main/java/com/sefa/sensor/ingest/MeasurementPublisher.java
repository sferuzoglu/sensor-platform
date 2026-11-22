package com.sefa.sensor.ingest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class MeasurementPublisher {

    private static final Logger log = LoggerFactory.getLogger(MeasurementPublisher.class);

    private final KafkaTemplate<String, Measurement> kafka;

    public MeasurementPublisher(KafkaTemplate<String, Measurement> kafka) {

        this.kafka = kafka;
    }

    void publish(Measurement m) {

        kafka.send(KafkaTopicConfig.SENSOR_MEASUREMENTS, String.valueOf(m.sensorId()), m).whenComplete((result, ex) -> {
            if (ex != null) {

                log.warn("Failed to publish measurement for sensor {}", m.sensorId(), ex);
            }
        });
    }

}
