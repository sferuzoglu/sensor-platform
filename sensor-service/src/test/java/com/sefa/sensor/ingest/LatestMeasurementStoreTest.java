package com.sefa.sensor.ingest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.sefa.sensor.TestcontainersConfiguration;

@SpringBootTest(properties = "sensor.ingest.udp-port=0")
@Import(TestcontainersConfiguration.class)
public class LatestMeasurementStoreTest {

    private static final long SENSOR_ID = 1;

    private static final double VALUE = 21.5;

    private static final String KEY = "sensor:1:latest";

    private static final Instant TIMESTAMP = Instant.parse("2026-01-01T00:00:00Z");

    @Autowired
    LatestMeasurementStore store;

    @Autowired
    StringRedisTemplate redisTemplate;

    @BeforeEach
    void cleanUp() {

        redisTemplate.execute((RedisCallback<Void>) connection -> {
            connection.serverCommands().flushDb();
            return null;
        });
    }

    @Test
    void first_packet_is_accepted() {

        assertTrue(store.save(measurement(1)));

        assertEquals("1", redisTemplate.opsForHash().get(KEY, "sequence"));

        assertEquals("1767225600000", redisTemplate.opsForHash().get(KEY, "timestamp"));

        assertEquals("21.5", redisTemplate.opsForHash().get(KEY, "value"));
    }

    @Test
    void older_packet_is_rejected() {

        assertTrue(store.save(measurement(8)));

        assertFalse(store.save(measurement(5)));

        assertEquals("8", redisTemplate.opsForHash().get(KEY, "sequence"));
    }

    @Test
    void duplicate_packet_is_rejected() {

        assertTrue(store.save(measurement(8)));

        assertFalse(store.save(measurement(8)));
    }

    @Test
    void packet_after_silence_is_accepted() {

        assertTrue(store.save(measurement(8)));

        assertTrue(redisTemplate.delete(KEY));

        assertTrue(store.save(measurement(1)));

        assertEquals("1", redisTemplate.opsForHash().get(KEY, "sequence"));
    }

    @Test
    void key_has_ttl() {

        assertTrue(store.save(measurement(1)));

        Long ttl = redisTemplate.getExpire(KEY);

        assertTrue(ttl > 0 && ttl <= 10, "Unexpected TTL: " + ttl);
    }

    @Test
    void latest_is_read_back() {

        assertTrue(store.save(measurement(1)));

        assertEquals(measurement(1), store.findLatest(SENSOR_ID).orElseThrow());
    }

    @Test
    void missing_key_returns_empty() {

        Optional<Measurement> opt = store.findLatest(SENSOR_ID);

        assertTrue(opt.isEmpty());
    }

    private Measurement measurement(long sequence) {

        return new Measurement(SENSOR_ID, sequence, TIMESTAMP, VALUE);
    }

}
