package net.darkslave.stm.client.simple.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import net.darkslave.stm.core.Client;
import net.darkslave.stm.core.ClientConfig;
import net.darkslave.stm.proto.Message;





public class UdpClient implements Client {
    private final ClientConfig config;
    private final DatagramSocket socket;


    public UdpClient(ClientConfig config) throws IOException {
        this.config = config;
        this.socket = new DatagramSocket();
    }


    @Override
    public void init() {
    }


    @Override
    public void send(String param, double value) throws IOException {
        byte[] buffer = Message.encode(param, value);

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
        socket.close();
    }

}


