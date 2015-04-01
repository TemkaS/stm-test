package net.darkslave.stm.client.temkas.tcp;


import net.darkslave.stm.core.Client;
import net.darkslave.stm.core.ClientConfig;
import net.darkslave.stm.proto.Message;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;





/**
 * Created by TemkaS on 31.03.2015.
 */
public class ClientImpl implements Client {

    private final ClientConfig config;


    public ClientImpl(ClientConfig config) throws IOException {
        this.config = config;
    }


    @Override
    public void init() throws IOException {
    }


    @Override
    public void send(Message messg) throws IOException {
        try (SocketChannel channel = SocketChannel.open()) {
            channel.configureBlocking(false);
            channel.connect(new InetSocketAddress(config.getTargetHost(), config.getTargetPort().get()));
            // TODO: wtf TemkaS ??
            while (!channel.finishConnect());
            ByteBuffer buf = ByteBuffer.wrap(Message.encode(messg));
            channel.write(buf);
        }
    }


    @Override
    public void close() throws IOException {
    }
}