package net.darkslave.stm.server.netty.udp;

import java.io.IOException;
import net.darkslave.stm.core.Server;
import net.darkslave.stm.core.ServerConfig;
import net.darkslave.stm.proto.MessageHandler;





public class UdpServer implements Server {
    private final ServerConfig config;


    public UdpServer(ServerConfig config) throws IOException {
        this.config = config;
    }


    @Override
    public void start() throws IOException {

    }


    @Override
    public void close() throws IOException {

    }


    @Override
    public void setHandler(MessageHandler handler) {


    }


}


