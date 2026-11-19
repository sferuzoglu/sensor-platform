package com.sefa.sensor.ingest;

import java.util.List;

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

        Long execute = redisTemplate.execute(SAVE_LATEST, List.of(keyGenerator(measurement)), sequenceStr,
                timestampInMilliSecondStr, valueStr, "10");

        return Long.valueOf(1).equals(execute);
    }

    private String keyGenerator(Measurement measurement) {

        return "sensor:" + measurement.sensorId() + ":latest";
    }

}
