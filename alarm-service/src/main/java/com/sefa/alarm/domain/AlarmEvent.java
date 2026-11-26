package com.sefa.alarm.domain;

public sealed interface AlarmEvent permits AlarmRaised, AlarmCleared {

}
