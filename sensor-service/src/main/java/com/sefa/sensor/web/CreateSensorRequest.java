package com.sefa.sensor.web;

import com.sefa.sensor.domain.SensorType;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateSensorRequest(
        @NotBlank(message = "Sensor name must not be blank") @Size(max = 200, message = "Sensor name must be at most 200 characters") String name,

        @NotNull(message = "Sensor type is required") SensorType type,

        @NotNull(message = "Latitude is required") @DecimalMin(value = "-90", message = "Latitude must be at least -90") @DecimalMax(value = "90", message = "Latitude must be at most 90") Double latitude,

        // Longitude must be in [-180, 180): -180 and 180 are the same meridian, so 180 is excluded.
        @NotNull(message = "Longitude is required") @DecimalMin(value = "-180", message = "Longitude must be at least -180") @DecimalMax(value = "180", inclusive = false, message = "Longitude must be less than 180") Double longitude) {

}
