package net.darkslave.stm.mynio.tcp.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.darkslave.nio.Bootstrap;
import net.darkslave.nio.ErrorHandler;
import net.darkslave.nio.RequestHandler;
import net.darkslave.nio.Server;
import net.darkslave.stm.core.Message;
import net.darkslave.stm.core.MessageHandler;
import net.darkslave.stm.core.ServerConfig;





public class ServerImpl implements net.darkslave.stm.core.Server {
    private static final Logger logger = LogManager.getLogger(ServerImpl.class);

    private final ServerConfig config;
    private MessageHandler handler;
    private final ExecutorService bossThreadPool;
    private final ExecutorService workThreadPool;
    private final List<Server> active;


    public ServerImpl(ServerConfig config) throws IOException {
        this.config = config;
        this.active = new LinkedList<>();
        this.bossThreadPool = Executors.newCachedThreadPool();
        this.workThreadPool = Executors.newCachedThreadPool();
    }


    @Override
    public void start() throws IOException {
        Bootstrap boot = new Bootstrap();
        boot.setBossThreadPool(bossThreadPool);
        boot.setWorkThreadPool(workThreadPool);
        boot.setPendingCount(16536);
        boot.setSelectorDelay(5);

        Worker worker = new Worker(handler);
        boot.setRequestHandler(worker);
        boot.setErrorHandler(worker);

        for (Integer port : config.getTargetPort()) {
            Server server = new net.darkslave.nio.impl.ServerImpl(boot.setAddress(port));
            server.start();
            active.add(server);
        }

    }


    @Override
    public void close() throws IOException {
        for (Server server : active)
            server.stop();

        bossThreadPool.shutdownNow();
        workThreadPool.shutdownNow();
    }


    @Override
    public void setHandler(MessageHandler handler) {
        this.handler = handler;
    }


    private static class Worker implements RequestHandler, ErrorHandler {
        private final MessageHandler handler;

        public Worker(MessageHandler handler) throws IOException {
            this.handler = handler;
        }

        @Override
        public void handle(InputStream input, OutputStream output) throws IOException {
            while (!Thread.interrupted()) {
                Message messg = Message.readFrom(input);

                if (messg == null)
                    break;

                handler.accept(messg);
            }
        }

        @Override
        public void handle(Exception e) {
            logger.catching(e);
        }

    }

}
