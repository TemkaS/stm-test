package net.darkslave.stm.simple.tcp.client;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import net.darkslave.stm.core.Client;
import net.darkslave.stm.core.ClientConfig;
import net.darkslave.stm.core.Message;





public class ClientImpl implements Client {
    private final ClientConfig config;
    private Socket socket;
    private OutputStream stream;


    public ClientImpl(ClientConfig config) throws IOException {
        this.config = config;
    }


    @Override
    public void init() throws IOException {
        socket = new Socket(config.getTargetHost(), config.getTargetPort().get());
        stream = socket.getOutputStream();
    }


    @Override
    public void send(Message messg) throws IOException {
        Message.writeTo(messg, stream);
    }


    @Override
    public void close() throws IOException {
        if (socket != null)
            socket.close();
    }

}
