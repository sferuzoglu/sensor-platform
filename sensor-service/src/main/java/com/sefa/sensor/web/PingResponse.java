package com.sefa.sensor.web;

import java.time.Instant;

public record PingResponse(String status, Instant time) {
}
