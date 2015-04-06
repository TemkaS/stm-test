package net.darkslave.stm.simple.udp.server;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.darkslave.stm.core.Message;
import net.darkslave.stm.core.MessageHandler;
import net.darkslave.stm.core.Server;
import net.darkslave.stm.core.ServerConfig;





public class ServerImpl implements Server {
    private static final Logger logger = LogManager.getLogger(ServerImpl.class);

    private final ServerConfig config;
    private final ExecutorService bossThreadPool;
    private final List<Worker> workers;
    private MessageHandler handler;


    public ServerImpl(ServerConfig config) throws IOException {
        this.config = config;
        this.workers = new LinkedList<>();
        this.bossThreadPool = Executors.newCachedThreadPool();
    }


    @Override
    public void setHandler(MessageHandler handler) {
        this.handler = handler;
    }


    @Override
    public void start() throws IOException {
        for (Integer port : config.getTargetPort()) {
            Worker worker = new Worker(port, handler);
            bossThreadPool.execute(worker);
            workers.add(worker);
        }
    }


    @Override
    public void close() throws IOException {
        for (Worker worker : workers)
            worker.close();

        bossThreadPool.shutdownNow();
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

                    Message messg = Message.read(packet.getData(), 0, packet.getLength());
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
