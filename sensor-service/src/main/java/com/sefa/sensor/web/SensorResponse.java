package com.sefa.sensor.web;

import java.time.Instant;

import com.sefa.sensor.domain.Sensor;
import com.sefa.sensor.domain.SensorType;

public record SensorResponse(Long id, String name, SensorType type, double latitude, double longitude, boolean active,
        Instant createdAt) {

    public static SensorResponse from(Sensor e) {

        return new SensorResponse(e.getId(), e.getName(), e.getType(), e.getLocation().latitude(),
                e.getLocation().longitude(), e.isActive(), e.getCreatedAt());
    }
}
