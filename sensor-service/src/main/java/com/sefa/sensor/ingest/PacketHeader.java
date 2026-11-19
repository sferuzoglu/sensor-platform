package com.sefa.sensor.ingest;

public record PacketHeader(int version, int messageType, int payloadLength, long sensorId, long sequence) {

}
