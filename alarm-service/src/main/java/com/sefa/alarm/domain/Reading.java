package com.sefa.alarm.domain;

import java.time.Instant;

public record Reading(long sensorId, long sequence, Instant timestamp, double value) {

}
