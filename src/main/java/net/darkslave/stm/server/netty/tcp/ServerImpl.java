package net.darkslave.stm.server.netty.tcp;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import net.darkslave.stm.core.Server;
import net.darkslave.stm.core.ServerConfig;
import net.darkslave.stm.proto.MessageHandler;





public class ServerImpl implements Server {
    private final ServerConfig config;
    private MessageHandler handler;

    private final List<NettyServer> active;


    public ServerImpl(ServerConfig config) throws IOException {
        this.config = config;
        this.active = new LinkedList<>();
    }


    @Override
    public void start() throws IOException {
        for (Integer port : config.getTargetPort()) {
            NettyServer worker = new NettyServer(port, handler);

            Thread thread = new Thread(worker);
            thread.setDaemon(true);
            thread.start();

            active.add(worker);
        }
    }


    @Override
    public void close() throws IOException {
        for (NettyServer worker : active) {
            worker.close();
        }
    }


    @Override
    public void setHandler(MessageHandler handler) {
        this.handler = handler;
    }


}


