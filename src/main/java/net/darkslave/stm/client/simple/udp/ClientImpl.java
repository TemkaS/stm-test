package net.darkslave.stm.client.simple.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import net.darkslave.stm.core.Client;
import net.darkslave.stm.core.ClientConfig;
import net.darkslave.stm.proto.Message;





public class ClientImpl implements Client {
    private final ClientConfig config;
    private DatagramSocket socket;


    public ClientImpl(ClientConfig config) throws IOException {
        this.config = config;
    }


    @Override
    public void init() throws IOException {
        socket = new DatagramSocket();
    }


    @Override
    public void send(Message messg) throws IOException {
        byte[] buffer = Message.encode(messg);

        DatagramPacket packet = new DatagramPacket(
                buffer,
                buffer.length,
                config.getTargetHost(),
                config.getTargetPort().get()
                );

        socket.send(packet);
    }


    @Override
    public void close() throws IOException {
        if (socket != null)
            socket.close();
    }

}


