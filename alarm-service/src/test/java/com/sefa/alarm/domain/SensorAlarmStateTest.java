package com.sefa.alarm.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

public class SensorAlarmStateTest {

    private static final long SENSOR_ID = 1;
    private static final Instant TS = Instant.parse("2026-01-01T00:00:00Z");
    private static final Threshold THRESHOLD = new Threshold(0, 50);

    @Test
    void normal_reading_produces_no_event() {

        SensorAlarmState state = new SensorAlarmState(SENSOR_ID);

        List<AlarmEvent> events = state.evaluate(reading(1, 25), THRESHOLD);

        assertEquals(List.of(), events);
    }

    @Test
    void high_reading_raises_alarm() {

        SensorAlarmState state = new SensorAlarmState(SENSOR_ID);

        List<AlarmEvent> events = state.evaluate(reading(1, 60), THRESHOLD);

        assertEquals(List.of(new AlarmRaised(SENSOR_ID, AlarmCondition.HIGH, 60.0, TS)), events);
    }

    @Test
    void repeated_high_reading_does_not_raise_again() {

        SensorAlarmState state = new SensorAlarmState(SENSOR_ID);

        state.evaluate(reading(1, 60), THRESHOLD);

        List<AlarmEvent> events = state.evaluate(reading(2, 70), THRESHOLD);

        assertEquals(List.of(), events);
    }

    @Test
    void return_to_normal_clears_alarm() {

        SensorAlarmState state = new SensorAlarmState(SENSOR_ID);

        state.evaluate(reading(1, 60), THRESHOLD);

        List<AlarmEvent> events = state.evaluate(reading(2, 25), THRESHOLD);

        assertEquals(List.of(new AlarmCleared(SENSOR_ID, AlarmCondition.HIGH, TS)), events);
    }

    @Test
    void nan_reading_raises_invalid() {

        SensorAlarmState state = new SensorAlarmState(SENSOR_ID);

        List<AlarmEvent> events = state.evaluate(reading(1, Double.NaN), THRESHOLD);

        assertEquals(List.of(new AlarmRaised(SENSOR_ID, AlarmCondition.INVALID, Double.NaN, TS)), events);
    }

    @Test
    void stale_reading_is_ignored() {

        SensorAlarmState state = new SensorAlarmState(SENSOR_ID);

        state.evaluate(reading(8, 25), THRESHOLD);

        List<AlarmEvent> events = state.evaluate(reading(5, 60), THRESHOLD);

        assertEquals(List.of(), events);

        assertEquals(List.of(new AlarmRaised(SENSOR_ID, AlarmCondition.HIGH, 60.0, TS)),
                state.evaluate(reading(9, 60), THRESHOLD));
    }

    @Test
    void high_to_low_produces_clear_and_raise() {

        SensorAlarmState state = new SensorAlarmState(SENSOR_ID);

        state.evaluate(reading(1, 60), THRESHOLD);

        List<AlarmEvent> events = state.evaluate(reading(2, -10), THRESHOLD);

        assertEquals(List.of(new AlarmCleared(SENSOR_ID, AlarmCondition.HIGH, TS),
                new AlarmRaised(SENSOR_ID, AlarmCondition.LOW, -10, TS)), events);
    }

    private Reading reading(long sequence, double value) {

        return new Reading(SENSOR_ID, sequence, TS, value);
    }
}
