package net.darkslave.stm.mynio.tcp.server;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.channels.ByteChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.darkslave.nio.core.Bootstrap;
import net.darkslave.nio.core.ErrorHandler;
import net.darkslave.nio.core.RequestHandler;
import net.darkslave.stm.core.Message;
import net.darkslave.stm.core.MessageAcceptor;
import net.darkslave.stm.core.Server;
import net.darkslave.stm.core.ServerConfig;





public class ServerImpl implements Server {
    private static final Logger logger = LogManager.getLogger(ServerImpl.class);

    private final ServerConfig config;
    private final ExecutorService bossThreadPool = Executors.newCachedThreadPool();
    private final ExecutorService workThreadPool = Executors.newCachedThreadPool();
    private net.darkslave.nio.core.Server server;
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

        boot.setServerOption(StandardSocketOptions.SO_RCVBUF, 16536);
        boot.setServerOption(StandardSocketOptions.SO_REUSEADDR, true);

        Worker worker = new Worker(handler);
        boot.setRequestHandler(worker);
        boot.setErrorHandler(worker);

        for (Integer port : config.getServerPort())
            boot.addAddress(port);

        net.darkslave.nio.core.Server server = new net.darkslave.nio.impl.ServerImpl(boot);
        server.start();

    }


    @Override
    public void close() throws IOException {
        if (server != null)
            server.stop();

        bossThreadPool.shutdownNow();
        workThreadPool.shutdownNow();

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
