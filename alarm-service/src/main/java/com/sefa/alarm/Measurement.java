package com.sefa.alarm;

import java.time.Instant;

public record Measurement(long sensorId, long sequence, Instant timestamp, double value) {

}
