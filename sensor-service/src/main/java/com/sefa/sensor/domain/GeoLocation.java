package com.sefa.sensor.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public record GeoLocation(double latitude, double longitude) {

    public GeoLocation {

        if (latitude < -90 || latitude > 90) {

            throw new IllegalArgumentException("enlem aralık dışında. enlem:" + latitude);
        } else if (longitude < -180 || longitude >= 180) {

            throw new IllegalArgumentException("boylam aralık dışında. boylam:" + longitude);
        }

    }

}
