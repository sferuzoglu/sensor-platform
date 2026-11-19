package com.sefa.sensor.ingest;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

@Component
public class LatestMeasurementStore {

    private static final RedisScript<Long> SAVE_LATEST = RedisScript
            .of(new ClassPathResource("scripts/save-latest.lua"), Long.class);

    private final StringRedisTemplate redisTemplate;

    public LatestMeasurementStore(StringRedisTemplate redisTemplate) {

        this.redisTemplate = redisTemplate;
    }

    public boolean save(Measurement measurement) {

        String sequenceStr = String.valueOf(measurement.sequence());
        String timestampInMilliSecondStr = String.valueOf(measurement.timestamp().toEpochMilli());
        String valueStr = String.valueOf(measurement.value());

        Long execute = redisTemplate.execute(SAVE_LATEST, List.of(keyFor(measurement.sensorId())), sequenceStr,
                timestampInMilliSecondStr, valueStr, "10");

        return Long.valueOf(1).equals(execute);
    }

    private String keyFor(long sensorId) {

        return "sensor:" + sensorId + ":latest";
    }

    public Optional<Measurement> findLatest(long sensorId) {

        Map<@NonNull Object, Object> entries = redisTemplate.opsForHash().entries(keyFor(sensorId));

        if (entries.isEmpty()) {

            return Optional.empty();
        }

        String sequence = (String) entries.get("sequence");
        String timestamp = (String) entries.get("timestamp");
        String value = (String) entries.get("value");

        Measurement measurement = new Measurement(sensorId, Long.parseLong(sequence),
                Instant.ofEpochMilli(Long.parseLong(timestamp)), Double.parseDouble(value));

        return Optional.of(measurement);
    }

}
