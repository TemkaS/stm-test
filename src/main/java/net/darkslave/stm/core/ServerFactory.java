package net.darkslave.stm.core;

import java.io.IOException;





public interface ServerFactory {


    Server create(ServerConfig config) throws IOException;


}
