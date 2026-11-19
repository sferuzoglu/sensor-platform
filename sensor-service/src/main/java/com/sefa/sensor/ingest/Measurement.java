package com.sefa.sensor.ingest;

import java.time.Instant;

public record Measurement(long sensorId, long sequence, Instant timestamp, double value) implements SensorMessage {

}
