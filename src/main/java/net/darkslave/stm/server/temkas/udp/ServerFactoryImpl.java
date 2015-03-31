package net.darkslave.stm.server.temkas.udp;

import net.darkslave.stm.core.Server;
import net.darkslave.stm.core.ServerConfig;
import net.darkslave.stm.core.ServerFactory;

import java.io.IOException;

/**
 * Created by TemkaS on 31.03.2015.
 */
public class ServerFactoryImpl implements ServerFactory {

    @Override
    public Server create(ServerConfig config) throws IOException {
        return new ServerImpl(config);
    }

}