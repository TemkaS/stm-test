package net.darkslave.stm.simple.tcp.server;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.darkslave.stm.core.Message;
import net.darkslave.stm.core.MessageAcceptor;
import net.darkslave.stm.core.Server;
import net.darkslave.stm.core.ServerConfig;





public class ServerImpl implements Server {
    private static final Logger logger = LogManager.getLogger(ServerImpl.class);

    private final ServerConfig config;
    private final ExecutorService bossThreadPool = Executors.newCachedThreadPool();
    private final ExecutorService workThreadPool = Executors.newCachedThreadPool();
    private final List<Closeable> workers = new LinkedList<>();
    private MessageAcceptor handler;


    public ServerImpl(ServerConfig config) throws IOException {
        this.config = config;
    }


    @Override
    public void setHandler(MessageAcceptor handler) {
        this.handler = handler;
    }


    @Override
    public void start() throws IOException {
        for (int port : config.getServerPort()) {
            Worker worker = new Worker(port);
            bossThreadPool.execute(worker);
            workers.add(worker);
        }
    }


    @Override
    public void close() throws IOException {
        try {
            for (Closeable worker : workers)
                worker.close();

        } finally {
            bossThreadPool.shutdownNow();
            workThreadPool.shutdownNow();
        }
    }


    private class Worker implements Runnable, Closeable {
        private final int port;
        private volatile boolean active = true;


        public Worker(int port) throws IOException {
            this.port = port;
        }


        @Override
        public void run() {
            try (ServerSocket server = new ServerSocket(port)) {

                while (active && !Thread.interrupted()) {
                    workThreadPool.execute(new ServerHandler(server.accept()));
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


    private class ServerHandler implements Runnable {
        private final Socket socket;


        public ServerHandler(Socket socket) {
            this.socket = socket;
        }


        @Override
        public void run() {
            try (
                Closeable __socket = socket;
                InputStream stream = socket.getInputStream();
            ) {

                while (!Thread.interrupted()) {
                    Message messg = Message.readFrom(stream);

                    if (messg == null)
                        break;

                    handler.accept(messg);
                }

            } catch (IOException e) {
                logger.catching(e);
            }
        }

    }

}
