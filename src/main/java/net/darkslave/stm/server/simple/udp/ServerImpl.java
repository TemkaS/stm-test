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





public class ServerImpl implements Server {
    private static final Logger logger = LogManager.getLogger(ServerImpl.class);

    private final ServerConfig config;
    private MessageHandler handler;

    private final List<Worker> active;


    public ServerImpl(ServerConfig config) throws IOException {
        this.config = config;
        this.active = new LinkedList<>();
    }


    @Override
    public void start() throws IOException {
        for (Integer port : config.getTargetPort()) {
            Worker worker = new Worker(port, handler);

            Thread thread = new Thread(worker);
            thread.setDaemon(true);
            thread.start();

            active.add(worker);
        }
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
            try (DatagramSocket socket = new DatagramSocket(port)) {

                while (!Thread.interrupted() && active) {
                    byte[] recv = new byte[4096];
                    DatagramPacket packet = new DatagramPacket(recv, recv.length);
                    socket.receive(packet);

                    Message messg = Message.decode(packet.getData(), packet.getLength());
                    handler.accept(messg);
                }

            } catch (IOException e) {
                logger.error(e);
            }
        }


        @Override
        public void close() throws IOException {
            active = false;
        }

    }


}


