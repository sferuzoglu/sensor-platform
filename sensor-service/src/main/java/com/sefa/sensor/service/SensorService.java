package com.sefa.sensor.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sefa.sensor.domain.Sensor;
import com.sefa.sensor.domain.SensorRepository;
import com.sefa.sensor.domain.SensorType;
import com.sefa.sensor.exceptions.DuplicateSensorNameException;

@Service
public class SensorService {

    private final SensorRepository repository;

    public SensorService(SensorRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Sensor create(String name, SensorType type, double latitude, double longitude)
            throws DuplicateSensorNameException {

        boolean existsByName = repository.existsByName(name);

        if (existsByName) {

            throw new DuplicateSensorNameException("Bu isimde bir sensör zaten var: " + name);
        }

        Sensor sensor = new Sensor(name, type, latitude, longitude, true);
        repository.save(sensor);

        return sensor;
    }

    @Transactional(readOnly = true)
    public List<Sensor> findAll() {

        return repository.findAll();
    }
}
