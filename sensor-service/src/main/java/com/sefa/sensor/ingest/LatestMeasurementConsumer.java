package com.sefa.sensor.ingest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class LatestMeasurementConsumer {

    private static final Logger log = LoggerFactory.getLogger(LatestMeasurementConsumer.class);

    private final LatestMeasurementStore store;

    public LatestMeasurementConsumer(LatestMeasurementStore store) {

        this.store = store;
    }

    @KafkaListener(topics = KafkaTopicConfig.SENSOR_MEASUREMENTS, groupId = "sensor-service-latest")
    void onMeasurement(Measurement measurement) {

        boolean isSaved = store.save(measurement);

        if (!isSaved) {

            log.debug("Stale measurement dropped: {}", measurement);
        }
    }

}
