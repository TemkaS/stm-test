package net.darkslave.stm.mynio.tcp.server;

import java.io.IOException;
import java.nio.channels.ByteChannel;
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
import net.darkslave.stm.core.MessageAcceptor;
import net.darkslave.stm.core.ServerConfig;





public class ServerImpl implements net.darkslave.stm.core.Server {
    private static final Logger logger = LogManager.getLogger(ServerImpl.class);

    private final ServerConfig config;
    private final ExecutorService bossThreadPool = Executors.newCachedThreadPool();
    private final ExecutorService workThreadPool = Executors.newCachedThreadPool();
    private final List<Server> active = new LinkedList<>();
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
        Bootstrap boot = new Bootstrap();
        boot.setBossThreadPool(bossThreadPool);
        boot.setWorkThreadPool(workThreadPool);
        boot.setPendingCount(1024);
        boot.setSelectorDelay(10);

        Worker worker = new Worker(handler);
        boot.setRequestHandler(worker);
        boot.setErrorHandler(worker);

        for (Integer port : config.getServerPort()) {
            Server server = new net.darkslave.nio.impl.ServerImpl(boot.setAddress(port));
            server.start();
            active.add(server);
        }

    }


    @Override
    public void close() throws IOException {
        try {
            for (Server server : active)
                server.stop();

        } finally {
            bossThreadPool.shutdownNow();
            workThreadPool.shutdownNow();
        }
    }


    private static class Worker implements RequestHandler, ErrorHandler {
        private final MessageAcceptor handler;

        public Worker(MessageAcceptor handler) throws IOException {
            this.handler = handler;
        }

        @Override
        public void handle(ByteChannel channel) throws IOException {
            while (!Thread.interrupted()) {
                Message messg = Message.readFrom(channel);

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
