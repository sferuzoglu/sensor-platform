package com.sefa.sensor.ingest;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sefa.sensor.exceptions.InvalidPacketException;

@Component
public class PacketCodec {

    private static final Logger log = LoggerFactory.getLogger(PacketCodec.class);

    private static final int MAGIC = 0x5346;
    private static final int HEADER_SIZE = 15;
    private static final int VERSION = 1;

    private final Map<Integer, PayloadDecoder<?>> decoders;

    public PacketCodec(List<PayloadDecoder<?>> decoderList) {

        this.decoders = decoderList.stream().collect(Collectors.toMap(
                PayloadDecoder::messageType,
                Function.identity()));

        log.info("Registered decoders: {}", decoders.keySet());
    }

    public SensorMessage decode(ByteBuffer buffer) {

        if (buffer.remaining() < HEADER_SIZE) {

            throw new InvalidPacketException("Packet too short: " + buffer.remaining() + " bytes");
        }

        int magic = Short.toUnsignedInt(buffer.getShort());
        if (magic != PacketCodec.MAGIC) {

            throw new InvalidPacketException(
                    String.format("Bad magic: 0x%04X (expected 0x%04X)", magic, PacketCodec.MAGIC));
        }

        int version = Byte.toUnsignedInt(buffer.get());
        if (version != PacketCodec.VERSION) {

            throw new InvalidPacketException(
                    "Unsupported version: " + version + " (expected " + PacketCodec.VERSION + ")");
        }

        int type = Short.toUnsignedInt(buffer.getShort());
        PayloadDecoder<?> decoder = decoders.get(type);
        if (decoder == null) {

            throw new InvalidPacketException("Unknown message type: " + type);
        }

        int payloadLength = Short.toUnsignedInt(buffer.getShort());
        if (payloadLength != decoder.payloadSize()) {

            throw new InvalidPacketException(
                    "Payload length mismatch for type " + type + ": " + payloadLength + " (expected "
                            + decoder.payloadSize() + ")");
        }

        long sensorId = Integer.toUnsignedLong(buffer.getInt());
        long sequence = Integer.toUnsignedLong(buffer.getInt());

        if (buffer.remaining() != payloadLength) {

            throw new InvalidPacketException(
                    "Packet size mismatch: " + buffer.remaining() + " payload bytes received (header says "
                            + payloadLength + ")");
        }

        PacketHeader header = new PacketHeader(version, type, payloadLength, sensorId, sequence);

        return decoder.decode(header, buffer.slice());
    }
}
