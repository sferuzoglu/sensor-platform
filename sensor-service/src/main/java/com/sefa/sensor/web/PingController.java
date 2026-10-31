package com.sefa.sensor.web;

import java.time.Instant;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController 
public class PingController {


    @GetMapping("/api/ping")
    public PingResponse getPing() {

        return new PingResponse("ok", Instant.now());
    }
}
