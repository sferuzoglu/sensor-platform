package com.sefa.alarm.domain;

public record Threshold(double low, double high) {

    public Threshold {

        if (!(low < high)) {

            throw new IllegalArgumentException("Low must be less than high. low: " + low + ", high: " + high);
        }
    }

}
