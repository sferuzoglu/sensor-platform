package com.sefa.sensor.ingest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioDatagramChannel;

@Component
public class UdpServer implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(UdpServer.class);

    private final LatestMeasurementStore store;
    private final PacketCodec codec;
    private final int port;

    private EventLoopGroup group;
    private Channel channel;
    private volatile boolean running;

    public UdpServer(PacketCodec codec, LatestMeasurementStore store, @Value("${sensor.ingest.udp-port}") int port) {
        this.codec = codec;
        this.store = store;
        this.port = port;
    }

    @Override
    public void start() {

        group = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());

        Bootstrap bootstrap = new Bootstrap()
                .group(group)
                .channel(NioDatagramChannel.class)
                .handler(new MeasurementHandler(codec, store));

        try {
            channel = bootstrap.bind(port).sync().channel();

            running = true;

            log.info("UDP server listening on port {}", port);

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while binding UDP port " + port, e);
        }
    }

    @Override
    public void stop() {

        channel.close().syncUninterruptibly();
        group.shutdownGracefully().syncUninterruptibly();

        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

}
