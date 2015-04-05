package net.darkslave.stm.server.mynio.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.darkslave.nio.Bootstrap;
import net.darkslave.nio.ErrorHandler;
import net.darkslave.nio.RequestHandler;
import net.darkslave.nio.Server;
import net.darkslave.stm.core.ServerConfig;
import net.darkslave.stm.proto.Message;
import net.darkslave.stm.proto.MessageHandler;





public class ServerImpl implements net.darkslave.stm.core.Server {
    private static final Logger logger = LogManager.getLogger(ServerImpl.class);

    private final ServerConfig config;
    private MessageHandler handler;

    private final List<Server> active;


    public ServerImpl(ServerConfig config) throws IOException {
        this.config = config;
        this.active = new LinkedList<>();
    }


    @Override
    public void start() throws IOException {
        Bootstrap boot = new Bootstrap();
        boot.setBossThreadPool(Executors.newCachedThreadPool());
        boot.setWorkThreadPool(Executors.newFixedThreadPool(32));

        Worker worker = new Worker(handler);
        boot.setRequestHandler(worker);
        boot.setErrorHandler(worker);

        for (Integer port : config.getTargetPort()) {
            Server server = boot.setAddress(port).create();
            server.start();
            active.add(server);
        }
    }


    @Override
    public void close() throws IOException {
        for (Server server : active) {
            server.stop();
        }
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
            byte[] recv = new byte[4096];
            int read = input.read(recv);

            Message messg = Message.decode(recv, read);
            handler.accept(messg);
        }

        @Override
        public void handle(Exception e) {
            logger.catching(e);
        }

    }


}


