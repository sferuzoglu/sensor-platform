package com.sefa.sensor.web;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.sefa.sensor.TestcontainersConfiguration;
import com.sefa.sensor.domain.GeoLocation;
import com.sefa.sensor.domain.Sensor;
import com.sefa.sensor.domain.SensorRepository;
import com.sefa.sensor.domain.SensorType;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class SensorConcurrencyTest {

    @Autowired
    SensorRepository sensorRepository;

    @BeforeEach
    void temizle() {
        sensorRepository.deleteAllInBatch();
    }

    @Test
    void eski_surumle_kayit_istisna_firlatir() throws Exception {

        Sensor save = sensorRepository
                .save(new Sensor("istasyon-1", SensorType.HUMIDITY, new GeoLocation(38, 32), true));

        Optional<Sensor> first = sensorRepository.findById(save.getId());

        Optional<Sensor> second = sensorRepository.findById(save.getId());

        Sensor firstSensor = first.get();
        Sensor secondSensor = second.get();

        firstSensor.deactivate();

        sensorRepository.save(firstSensor);

        secondSensor.deactivate();

        assertThrows(ObjectOptimisticLockingFailureException.class, () -> sensorRepository.save(secondSensor));
    }

}