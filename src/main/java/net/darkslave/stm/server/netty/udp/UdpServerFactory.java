package net.darkslave.stm.server.netty.udp;

import java.io.IOException;
import net.darkslave.stm.core.Server;
import net.darkslave.stm.core.ServerConfig;
import net.darkslave.stm.core.ServerFactory;




public class UdpServerFactory implements ServerFactory {

    @Override
    public Server create(ServerConfig config) throws IOException {
        return new UdpServer(config);
    }

}
