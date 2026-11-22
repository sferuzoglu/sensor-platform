package com.sefa.sensor.ingest;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sefa.sensor.exceptions.InvalidPacketException;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

public class MeasurementHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private static final Logger log = LoggerFactory.getLogger(MeasurementHandler.class);

    private final PacketCodec codec;

    private final MeasurementPublisher measurementPublisher;

    public MeasurementHandler(PacketCodec codec,
            MeasurementPublisher measurementPublisher) {

        this.codec = codec;
        this.measurementPublisher = measurementPublisher;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {

        ByteBuffer buffer = packet.content().nioBuffer();

        try {

            SensorMessage message = codec.decode(buffer);

            log.debug("Received {}", message);

            if (message instanceof Measurement measurement) {

                measurementPublisher.publish(measurement);
            }
        } catch (InvalidPacketException e) {

            log.warn("Invalid packet from {}: {}", packet.sender(), e.getMessage());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

        log.error("Unexpected error in UDP handler", cause);
    }

}
