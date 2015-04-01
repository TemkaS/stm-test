package net.darkslave.stm.client.temkas.udp;


import net.darkslave.stm.core.Client;
import net.darkslave.stm.core.ClientConfig;
import net.darkslave.stm.proto.Message;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;





/**
 *
 * Created by TemkaS on 31.03.2015.
 */
public class ClientImpl implements Client {

    private DatagramChannel channel;
    private InetSocketAddress address;


    public ClientImpl(ClientConfig config) throws IOException {
        this.address = new InetSocketAddress(config.getTargetHost(), config.getTargetPort().get());
    }


    @Override
    public void init() throws IOException {
        channel = DatagramChannel.open();
        channel.configureBlocking(false);
        channel.connect(address);
    }


    @Override
    public void send(Message messg) throws IOException {
        ByteBuffer buf = ByteBuffer.wrap(Message.encode(messg));
        channel.send(buf, address);
    }


    @Override
    public void close() throws IOException {
        if (channel != null)
            channel.close();
    }
}