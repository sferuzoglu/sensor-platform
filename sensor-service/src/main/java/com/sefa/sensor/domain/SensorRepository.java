package com.sefa.sensor.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorRepository extends JpaRepository<Sensor, Long> {

    List<Sensor> findByType(SensorType type);

    boolean existsByName(String name);

}
