package net.darkslave.stm.client.reused.tcp;

import java.io.IOException;
import java.net.Socket;
import net.darkslave.stm.core.Client;
import net.darkslave.stm.core.ClientConfig;
import net.darkslave.stm.proto.Message;





public class ClientImpl implements Client {
    private final ClientConfig config;
    private Socket socket;


    public ClientImpl(ClientConfig config) throws IOException {
        this.config = config;
    }


    @Override
    public void init() throws IOException {
        socket = new Socket(config.getTargetHost(), config.getTargetPort().get());
    }


    @Override
    public void send(Message messg) throws IOException {
        byte[] buffer = Message.encode(messg);
        socket.getOutputStream().write(buffer);
    }


    @Override
    public void close() throws IOException {
        if (socket != null)
            socket.close();
    }

}


