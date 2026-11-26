package com.sefa.alarm.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class ThresholdTest {

    @Test
    void low_equal_to_high_is_rejected() {

        assertThrows(IllegalArgumentException.class, () -> new Threshold(10, 10));
    }

    @Test
    void nan_is_rejected() {

        assertThrows(IllegalArgumentException.class, () -> new Threshold(Double.NaN, 10));
    }

    @Test
    void valid_threshold_is_created() {

        Threshold threshold = assertDoesNotThrow(() -> new Threshold(0, 50));
        assertEquals(0, threshold.low());
        assertEquals(50, threshold.high());
    }

    @Test
    void low_greater_than_high_is_rejected() {

        assertThrows(IllegalArgumentException.class, () -> new Threshold(50, 10));
    }

}
