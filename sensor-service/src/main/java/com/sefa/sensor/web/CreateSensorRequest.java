package com.sefa.sensor.web;

import com.sefa.sensor.domain.SensorType;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateSensorRequest(
        @NotBlank(message = "Sensor adı boş olamaz") @Size(max = 200, message = "Sensor adı maksimum 200 karakter olabilir") String name,

        @NotNull(message = "Sensor Tipi boş olamaz") SensorType type,

        @NotNull(message = "Enlem boş olamaz") @DecimalMin(value = "-90", message = "enlem minimum değeri -90 olabilir") @DecimalMax(value = "90", message = "enlem maksimum değeri 90 olabilir") Double latitude,

        // boylamda degeri [-180,180) aralığında olması gerekiyor bu yüzden 180 açık
        // aralık.
        @NotNull(message = "Boylam boş olamaz") @DecimalMin(value = "-180", message = "boylam minimum değeri -180 olabilir") @DecimalMax(value = "180", inclusive = false, message = "boylam minimum değeri 180 olabilir") Double longitude) {

}
