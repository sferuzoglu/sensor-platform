package com.sefa.sensor.ingest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sefa.sensor.exceptions.InvalidPacketException;

public class PacketCodecTest {

    private final PacketCodec codec = new PacketCodec(List.of(new MeasurementDecoder()));

    @Test
    void valid_packet_is_decoded() {

        SensorMessage decodedMessage = codec.decode(validPacket());

        Measurement measurement = assertInstanceOf(Measurement.class, decodedMessage);

        assertEquals(SENSOR_ID, measurement.sensorId());

        assertEquals(SEQUENCE, measurement.sequence());

        assertEquals(TIMESTAMP, measurement.timestamp());

        assertEquals(VALUE, measurement.value());
    }

    @Test
    void unsigned_fields_are_decoded() {

        SensorMessage decodedMessage = codec
                .decode(packet(0x5346, 1, 1, 16, 0xFFFFFFFFL, 0x80000000L, TIMESTAMP, VALUE));

        Measurement measurement = assertInstanceOf(Measurement.class, decodedMessage);

        assertEquals(4294967295L, measurement.sensorId());

        assertEquals(2147483648L, measurement.sequence());
    }

    @Test
    void bad_magic_is_rejected() {

        InvalidPacketException ex = assertThrows(InvalidPacketException.class,
                () -> codec.decode(packet(0x1234, 1, 1, 16, SENSOR_ID, SEQUENCE, TIMESTAMP, VALUE)));
        assertTrue(ex.getMessage().contains("Bad magic"));
    }

    @Test
    void unknown_type_is_rejected() {

        InvalidPacketException ex = assertThrows(InvalidPacketException.class,
                () -> codec.decode(packet(0x5346, 1, 7, 16, SENSOR_ID, SEQUENCE, TIMESTAMP, VALUE)));
        assertTrue(ex.getMessage().contains("Unknown message type"));
    }

    @Test
    void payload_length_mismatch_is_rejected() {

        InvalidPacketException ex = assertThrows(InvalidPacketException.class,
                () -> codec.decode(packet(0x5346, 1, 1, 20, SENSOR_ID, SEQUENCE, TIMESTAMP, VALUE)));
        assertTrue(ex.getMessage().contains("Payload length mismatch"));
    }

    @Test
    void extra_bytes_are_rejected() {

        ByteBuffer packetWithExtraByte = ByteBuffer.allocate(32).put(validPacket()).put((byte) 0).flip();

        InvalidPacketException ex = assertThrows(InvalidPacketException.class,
                () -> codec.decode(packetWithExtraByte));
        assertTrue(ex.getMessage().contains("Packet size mismatch"));
    }

    @Test
    void unsupported_version_is_rejected() {

        InvalidPacketException ex = assertThrows(InvalidPacketException.class,
                () -> codec.decode(packet(0x5346, 2, 1, 16, SENSOR_ID, SEQUENCE, TIMESTAMP, VALUE)));
        assertTrue(ex.getMessage().contains("Unsupported version"));
    }

    @Test
    void packet_shorter_than_header_is_rejected() {

        InvalidPacketException ex = assertThrows(InvalidPacketException.class,
                () -> codec.decode(validPacket().limit(14)));
        assertTrue(ex.getMessage().contains("too short"));
    }

    // Example packet from the ICD (docs/protocol.md, section 3)
    static final long SENSOR_ID = 42;
    static final long SEQUENCE = 7;
    static final Instant TIMESTAMP = Instant.parse("2026-01-01T00:00:00Z");
    static final double VALUE = 21.5;

    private static ByteBuffer validPacket() {
        return packet(0x5346, 1, 1, 16, SENSOR_ID, SEQUENCE, TIMESTAMP, VALUE);
    }

    private static ByteBuffer packet(int magic, int version, int type, int payloadLength,
            long sensorId, long sequence, Instant timestamp, double value) {

        ByteBuffer buffer = ByteBuffer.allocate(31); // big-endian by default

        buffer.putShort((short) magic); // uint16
        buffer.put((byte) version); // uint8
        buffer.putShort((short) type); // uint16
        buffer.putShort((short) payloadLength); // uint16
        buffer.putInt((int) sensorId); // uint32
        buffer.putInt((int) sequence); // uint32
        buffer.putLong(timestamp.toEpochMilli()); // int64
        buffer.putDouble(value); // float64

        buffer.flip();
        return buffer;
    }

}
