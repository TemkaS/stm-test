package net.darkslave.stm.client.temkas.tcp;


import net.darkslave.stm.core.Client;
import net.darkslave.stm.core.ClientConfig;
import net.darkslave.stm.core.ClientFactory;
import java.io.IOException;





/**
 * Created by TemkaS on 31.03.2015.
 */
public class ClientFactoryImpl implements ClientFactory {

    @Override
    public Client create(ClientConfig config) throws IOException {
        return new ClientImpl(config);
    }

}