package com.sefa.alarm.domain;

import java.time.Instant;

public record AlarmCleared(long sensorId, AlarmCondition previousCondition, Instant timestamp)
        implements AlarmEvent {

}
