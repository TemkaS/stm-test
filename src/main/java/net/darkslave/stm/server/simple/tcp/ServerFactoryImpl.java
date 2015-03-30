package net.darkslave.stm.server.simple.tcp;

import java.io.IOException;
import net.darkslave.stm.core.Server;
import net.darkslave.stm.core.ServerConfig;
import net.darkslave.stm.core.ServerFactory;




public class ServerFactoryImpl implements ServerFactory {

    @Override
    public Server create(ServerConfig config) throws IOException {
        return new ServerImpl(config);
    }

}
