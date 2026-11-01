package com.sefa.sensor.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sefa.sensor.domain.GeoLocation;
import com.sefa.sensor.domain.Sensor;
import com.sefa.sensor.domain.SensorRepository;
import com.sefa.sensor.domain.SensorType;
import com.sefa.sensor.exceptions.DuplicateSensorNameException;
import com.sefa.sensor.exceptions.SensorNotFoundException;

@Service
public class SensorService {

    private final SensorRepository repository;

    public SensorService(SensorRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Sensor create(String name, SensorType type, GeoLocation location)
            throws DuplicateSensorNameException {

        boolean existsByName = repository.existsByName(name);

        if (existsByName) {

            throw new DuplicateSensorNameException("Bu isimde bir sensör zaten var: " + name);
        }

        Sensor sensor = new Sensor(name, type, location, true);
        repository.save(sensor);

        return sensor;
    }

    @Transactional(readOnly = true)
    public List<Sensor> findAll() {

        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Sensor findById(Long id) {

        Optional<Sensor> sensorOpt = repository.findById(id);

        return sensorOpt.orElseThrow(() -> new SensorNotFoundException("Sensor bulunamadi: id=" + id));
    }

    @Transactional
    public Sensor deactivate(Long id) {

        Optional<Sensor> sensorOpt = repository.findById(id);

        Sensor sensor = sensorOpt.orElseThrow(() -> new SensorNotFoundException("Sensor bulunamadi: id=" + id));

        sensor.deactivate();

        return sensor;

    }
}
