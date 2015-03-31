package net.darkslave.stm.server.netty.tcp;

import java.io.Closeable;
import java.io.IOException;
import net.darkslave.stm.proto.MessageHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;




public class NettyServer implements Runnable, Closeable {
    private final EventLoopGroup groupBoss;
    private final EventLoopGroup groupWork;

    private final int port;
    private final MessageHandler handler;

    private Channel channel;


    public NettyServer(int port, MessageHandler handler) {
        this.port = port;
        this.handler = handler;

        this.groupBoss = new NioEventLoopGroup();
        this.groupWork = new NioEventLoopGroup();

    }


    @Override
    public void run() {
        ServerBootstrap boot = new ServerBootstrap();

        boot.group(groupBoss, groupWork);

        boot.channel(NioServerSocketChannel.class);
        boot.option(ChannelOption.SO_BACKLOG, 16536);

        boot.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel chan) throws Exception {
                chan.pipeline().addLast(new NettyServerHandler(handler));
            }
        });

        channel = boot.bind(port).channel();
    }


    @Override
    public void close() throws IOException {
        channel.close();
        groupBoss.shutdownGracefully();
        groupWork.shutdownGracefully();
    }


}
