package net.darkslave.stm.netty.tcp.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import net.darkslave.stm.core.MessageAcceptor;
import net.darkslave.stm.core.Server;
import net.darkslave.stm.core.ServerConfig;





public class ServerImpl implements Server {
    private final ServerConfig config;
    private MessageAcceptor handler;
    private final EventLoopGroup groupBoss;
    private final EventLoopGroup groupWork;
    private final List<Channel> workers;


    public ServerImpl(ServerConfig config) throws IOException {
        this.config = config;
        this.workers = new LinkedList<>();
        this.groupBoss = new NioEventLoopGroup();
        this.groupWork = new NioEventLoopGroup();
    }


    @Override
    public void setHandler(MessageAcceptor handler) {
        this.handler = handler;
    }


    @Override
    public void start() throws IOException {
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

        for (Integer port : config.getServerPort()) {
            workers.add(boot.bind(port).channel());
        }

    }


    @Override
    public void close() throws IOException {
        for (Channel worker : workers)
            worker.close();

        groupBoss.shutdownGracefully();
        groupWork.shutdownGracefully();
    }

}

