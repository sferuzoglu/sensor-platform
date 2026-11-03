package com.sefa.sensor.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class GeoLocationTest {

    @Test
    void valid_location_is_created() {

        assertEquals(38, new GeoLocation(38, 33).latitude());
        assertEquals(33, new GeoLocation(38, 33).longitude());
    }

    @Test
    void invalid_latitude_throws() {

        assertThrows(IllegalArgumentException.class,
                () -> new GeoLocation(500, 32));
    }

    @Test
    void invalid_longitude_throws() {

        assertThrows(IllegalArgumentException.class,
                () -> new GeoLocation(38, 180));
    }

}
