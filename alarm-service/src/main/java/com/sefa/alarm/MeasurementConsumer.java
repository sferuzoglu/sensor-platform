package com.sefa.alarm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MeasurementConsumer {

    private static final Logger log = LoggerFactory.getLogger(MeasurementConsumer.class);

    @KafkaListener(topics = "sensor-measurements", groupId = "alarm-service")
    void onMeasurement(Measurement measurement) {

        log.debug("Received {}", measurement);
    }
}
