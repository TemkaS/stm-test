package net.darkslave.stm.core;

import java.io.IOException;





public interface ClientFactory {


    Client create(ClientConfig config) throws IOException;


}
