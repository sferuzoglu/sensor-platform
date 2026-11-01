package com.sefa.sensor.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class GeoLocationTest {

    @Test
    void gecerli_konum_ekle() {

        assertEquals(38, new GeoLocation(38, 33).latitude());
        assertEquals(33, new GeoLocation(38, 33).longitude());
    }

    @Test
    void gecersiz_enlem_ekle() {

        assertThrows(IllegalArgumentException.class,
                () -> new GeoLocation(500, 32));
    }

    @Test
    void gecersiz_boylam_ekle() {

        assertThrows(IllegalArgumentException.class,
                () -> new GeoLocation(38, 180));
    }

}
