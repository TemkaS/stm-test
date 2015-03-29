package net.darkslave.stm.server.simple.udp;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.darkslave.stm.core.Server;
import net.darkslave.stm.core.ServerConfig;
import net.darkslave.stm.proto.Message;
import net.darkslave.stm.proto.MessageHandler;





public class UdpServer implements Server {
    private static final Logger logger = LogManager.getLogger(UdpServer.class);

    private final ServerConfig config;
    private MessageHandler handler;

    private final List<Worker> active;


    public UdpServer(ServerConfig config) throws IOException {
        this.config = config;
        this.active = new LinkedList<>();
    }


    @Override
    public void start() throws IOException {
        Worker worker = new Worker(config.getTargetPort().get(), handler);

        Thread thread = new Thread(worker);
        thread.setDaemon(true);
        thread.start();

        active.add(worker);
    }


    @Override
    public void close() throws IOException {
        for (Worker worker : active) {
            worker.close();
        }
    }


    @Override
    public void setHandler(MessageHandler handler) {
        this.handler = handler;
    }


    private static class Worker implements Runnable, Closeable {
        private final int port;
        private final MessageHandler handler;
        private volatile boolean active = true;


        public Worker(int port, MessageHandler handler) throws IOException {
            this.port    = port;
            this.handler = handler;
        }


        @Override
        public void run() {
            DatagramSocket socket = null;

            try {
                socket = new DatagramSocket(port);

                while (!Thread.interrupted() && active) {
                    byte[] recv = new byte[4096];
                    DatagramPacket packet = new DatagramPacket(recv, recv.length);
                    socket.receive(packet);

                    Message messg = Message.decode(packet.getData());
                    handler.accept(messg);
                }

            } catch (IOException e) {
                logger.error(e);

            } finally {
                if (socket != null)
                    socket.close();
            }
        }


        @Override
        public void close() throws IOException {
            active = false;
        }

    }


}


