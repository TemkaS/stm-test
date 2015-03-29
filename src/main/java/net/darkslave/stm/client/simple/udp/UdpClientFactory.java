package net.darkslave.stm.client.simple.udp;

import java.io.IOException;
import net.darkslave.stm.core.Client;
import net.darkslave.stm.core.ClientConfig;
import net.darkslave.stm.core.ClientFactory;




public class UdpClientFactory implements ClientFactory {

    @Override
    public Client create(ClientConfig config) throws IOException {
        return new UdpClient(config);
    }

}
