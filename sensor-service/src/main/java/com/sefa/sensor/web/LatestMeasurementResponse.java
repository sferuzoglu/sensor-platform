package com.sefa.sensor.web;

import java.time.Instant;

import com.sefa.sensor.ingest.Measurement;

public record LatestMeasurementResponse(long sequence, Instant timestamp, double value) {

    public static LatestMeasurementResponse from(Measurement m) {

        return new LatestMeasurementResponse(m.sequence(), m.timestamp(), m.value());
    }
}
