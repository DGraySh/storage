package ru.gb.cloud_storage.storage_client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import ru.gb.cloud_storage.storage_common.ByteToMessageInboundHandler;
import ru.gb.cloud_storage.storage_common.CallMeBack;
import ru.gb.cloud_storage.storage_common.MessageToByteOutboundHandler;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;

public class Network {
    private static final Network ourInstance = new Network();
    private Channel currentChannel;

    private Network() {
    }

    public static Network getInstance() {
        return ourInstance;
    }

    public Channel getCurrentChannel() {
        return currentChannel;
    }

    public void start(CountDownLatch countDownLatch, CallMeBack cb) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap clientBootstrap = new Bootstrap();
            clientBootstrap.group(group);
            clientBootstrap.channel(NioSocketChannel.class);
            clientBootstrap.remoteAddress(new InetSocketAddress("localhost", 8186));
            clientBootstrap.handler(new ChannelInitializer<SocketChannel>() {
                protected void initChannel(SocketChannel socketChannel) {
                    socketChannel.pipeline().addLast(new MessageToByteOutboundHandler(), new ByteToMessageInboundHandler(), new ClientHandler(cb));
                    currentChannel = socketChannel;
                }
            });
            ChannelFuture channelFuture = clientBootstrap.connect().sync();
            countDownLatch.countDown();
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                group.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        currentChannel.close();
    }
}
