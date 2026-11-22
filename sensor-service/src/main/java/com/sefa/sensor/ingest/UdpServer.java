package com.sefa.sensor.ingest;

import java.net.InetSocketAddress;

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

    private final PacketCodec codec;
    private final int port;
    private final MeasurementPublisher measurementPublisher;

    private EventLoopGroup group;
    private Channel channel;
    private volatile boolean running;

    public UdpServer(PacketCodec codec, MeasurementPublisher measurementPublisher,
            @Value("${sensor.ingest.udp-port}") int port) {
        this.codec = codec;
        this.measurementPublisher = measurementPublisher;
        this.port = port;
    }

    @Override
    public void start() {

        group = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());

        Bootstrap bootstrap = new Bootstrap()
                .group(group)
                .channel(NioDatagramChannel.class)
                .handler(new MeasurementHandler(codec, measurementPublisher));

        try {
            channel = bootstrap.bind(port).sync().channel();

            running = true;

            int realPort = ((InetSocketAddress) channel.localAddress()).getPort();

            log.info("UDP server listening on port {}", realPort);

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
