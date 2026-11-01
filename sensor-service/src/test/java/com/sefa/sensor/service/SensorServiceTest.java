package com.sefa.sensor.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sefa.sensor.domain.GeoLocation;
import com.sefa.sensor.domain.Sensor;
import com.sefa.sensor.domain.SensorRepository;
import com.sefa.sensor.domain.SensorType;
import com.sefa.sensor.exceptions.DuplicateSensorNameException;
import com.sefa.sensor.exceptions.SensorNotFoundException;

@ExtendWith(MockitoExtension.class)
public class SensorServiceTest {

    @Mock
    SensorRepository repository; // sahte bağımlılık

    @InjectMocks
    SensorService service; // sahteyi constructor'a enjekte eder

    @Test
    void isim_kullanilmiyorsa_ekle() {

        when(repository.existsByName("istasyon_1")).thenReturn(false);
        Sensor sensor = service.create("istasyon_1", SensorType.HUMIDITY, new GeoLocation(38, 33));

        assertEquals("istasyon_1", sensor.getName());

        verify(repository).save(any());
    }

    @Test
    void isim_kullaniliyorsa_ekleme() {

        when(repository.existsByName("istasyon_1")).thenReturn(true);

        assertThrows(DuplicateSensorNameException.class,
                () -> service.create("istasyon_1", SensorType.HUMIDITY, new GeoLocation(38, 33)));

        verify(repository, never()).save(any());
    }

    @Test
    void olmayan_id_testi() {

        when(repository.findById(7L)).thenReturn(Optional.empty());

        assertThrows(SensorNotFoundException.class,
                () -> service.findById(7L));
    }

}
