package com.sefa.alarm.domain;

import java.util.ArrayList;
import java.util.List;

public class SensorAlarmState {

    private final long sensorId;
    private AlarmCondition condition = AlarmCondition.NORMAL;
    private long lastSequence = -1;

    public SensorAlarmState(long sensorId) {

        this.sensorId = sensorId;
    }

    public List<AlarmEvent> evaluate(Reading reading, Threshold threshold) {

        List<AlarmEvent> eventList = new ArrayList<>();

        if (reading.sequence() <= lastSequence) {

            return eventList;
        }

        AlarmCondition newCondition = null;

        lastSequence = reading.sequence();

        double value = reading.value();

        if (Double.isNaN(value) || Double.isInfinite(value)) {

            newCondition = AlarmCondition.INVALID;
        } else if (value > threshold.high()) {

            newCondition = AlarmCondition.HIGH;
        } else if (value < threshold.low()) {

            newCondition = AlarmCondition.LOW;
        } else {

            newCondition = AlarmCondition.NORMAL;
        }

        if (!condition.equals(newCondition)) {

            if (condition != AlarmCondition.NORMAL) {

                eventList.add(new AlarmCleared(sensorId, condition, reading.timestamp()));
            }
            if (newCondition != AlarmCondition.NORMAL) {

                eventList.add(new AlarmRaised(sensorId, newCondition, value, reading.timestamp()));
            }
            condition = newCondition;
        }

        return eventList;
    }
}
