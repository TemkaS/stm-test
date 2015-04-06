package net.darkslave.stm.simple.tcp.server;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.darkslave.stm.core.MessageHandler;





public class ServerWorker  implements Runnable, Closeable {
    static final Logger logger = LogManager.getLogger(ServerWorker.class);

    private ExecutorService workThreadPool;
    private MessageHandler handler;
    private int port;

    private volatile boolean active = true;


    public void setWorkThreadPool(ExecutorService workThreadPool) {
        this.workThreadPool = workThreadPool;
    }


    public void setHandler(MessageHandler handler) {
        this.handler = handler;
    }


    public void setPort(int port) {
        this.port = port;
    }


    @Override
    public void run() {
        try (ServerSocket server = new ServerSocket(port)) {

            while (active && !Thread.interrupted()) {
                workThreadPool.execute(new ServerHandler(handler, server.accept()));
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
