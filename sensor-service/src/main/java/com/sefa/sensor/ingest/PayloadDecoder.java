package com.sefa.sensor.ingest;

import java.nio.ByteBuffer;

public interface PayloadDecoder<T extends SensorMessage> {

    int messageType();

    int payloadSize();

    T decode(PacketHeader header, ByteBuffer payload);
}
