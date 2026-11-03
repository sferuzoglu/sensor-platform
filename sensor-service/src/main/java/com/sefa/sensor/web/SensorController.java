package com.sefa.sensor.web;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.sefa.sensor.domain.GeoLocation;
import com.sefa.sensor.domain.Sensor;
import com.sefa.sensor.service.SensorService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/sensors")
public class SensorController {

    private final SensorService service;

    public SensorController(SensorService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SensorResponse> create(@RequestBody @Valid CreateSensorRequest req) {

        Sensor sensor = service.create(req.name(), req.type(), new GeoLocation(req.latitude(), req.longitude()));
        SensorResponse sensorResponse = SensorResponse.from(sensor);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(sensor.getId()).toUri();

        return ResponseEntity.created(location).body(sensorResponse);
    }

    @GetMapping
    public List<SensorResponse> findAll() {

        return service.findAll().stream().map(SensorResponse::from).toList();
    }

    @GetMapping("/{id}")
    public SensorResponse findById(@PathVariable Long id) {

        return SensorResponse.from(service.findById(id));
    }

    @PostMapping("/{id}/deactivate")
    public SensorResponse deactivate(@PathVariable Long id) {

        return SensorResponse.from(service.deactivate(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeSensor(@PathVariable Long id) {

        service.removeById(id);
    }

}
