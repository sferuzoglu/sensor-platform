package com.sefa.sensor.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.sefa.sensor.TestcontainersConfiguration;
import com.sefa.sensor.domain.GeoLocation;
import com.sefa.sensor.domain.Sensor;
import com.sefa.sensor.domain.SensorRepository;
import com.sefa.sensor.domain.SensorType;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class SensorApiIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    SensorRepository sensorRepository;

    @BeforeEach
    void cleanUp() {
        sensorRepository.deleteAllInBatch();
    }

    @Test
    void valid_request_returns_201() throws Exception {

        mockMvc.perform(post("/api/sensors").contentType(MediaType.APPLICATION_JSON).content("""
                {   "name": "station-1",
                    "type": "HUMIDITY",
                    "latitude": 41.01,
                    "longitude": 28.97}
                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void duplicate_name_returns_409() throws Exception {

        sensorRepository.save(new Sensor("station-1", SensorType.HUMIDITY, new GeoLocation(38, 33), true));

        mockMvc.perform(post("/api/sensors").contentType(MediaType.APPLICATION_JSON).content("""
                {   "name": "station-1",
                    "type": "HUMIDITY",
                    "latitude": 41.01,
                    "longitude": 28.97}
                """))
                .andExpect(status().isConflict());
    }

    @Test
    void invalid_parameter_returns_400() throws Exception {

        mockMvc.perform(post("/api/sensors").contentType(MediaType.APPLICATION_JSON).content("""
                {   "name": "station-1",
                    "type": "HUMIDITY",
                    "latitude": 500,
                    "longitude": 28.97}
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.latitude").exists());
    }

    @Test
    void unknown_id_returns_404() throws Exception {

        mockMvc.perform(get("/api/sensors/99999"))
                .andExpect(status().isNotFound());
    }

}
