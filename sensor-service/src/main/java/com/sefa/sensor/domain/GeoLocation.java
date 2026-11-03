package com.sefa.sensor.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public record GeoLocation(double latitude, double longitude) {

    public GeoLocation {

        if (latitude < -90 || latitude > 90) {

            throw new IllegalArgumentException("Latitude out of range: " + latitude);
        } else if (longitude < -180 || longitude >= 180) {

            throw new IllegalArgumentException("Longitude out of range: " + longitude);
        }

    }

}
