package com.sefa.alarm.domain;

import java.time.Instant;

public record AlarmRaised(long sensorId, AlarmCondition condition, double value, Instant timestamp)
        implements AlarmEvent {

}
