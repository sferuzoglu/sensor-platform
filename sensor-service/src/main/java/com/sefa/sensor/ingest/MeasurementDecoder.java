package com.sefa.sensor.ingest;

import java.nio.ByteBuffer;
import java.time.Instant;

import org.springframework.stereotype.Component;

@Component
public class MeasurementDecoder implements PayloadDecoder<Measurement> {

    public static final int MESSAGE_TYPE = 1;
    public static final int PAYLOAD_SIZE = 16;

    @Override
    public int messageType() {

        return MESSAGE_TYPE;
    }

    @Override
    public int payloadSize() {

        return PAYLOAD_SIZE;
    }

    @Override
    public Measurement decode(PacketHeader header, ByteBuffer payload) {

        Instant timestamp = Instant.ofEpochMilli(payload.getLong());

        double value = payload.getDouble();

        return new Measurement(header.sensorId(), header.sequence(), timestamp, value);
    }

}
