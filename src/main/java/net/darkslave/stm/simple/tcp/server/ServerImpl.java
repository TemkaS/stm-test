package net.darkslave.stm.simple.tcp.server;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.darkslave.stm.core.MessageHandler;
import net.darkslave.stm.core.Server;
import net.darkslave.stm.core.ServerConfig;





public class ServerImpl implements Server {
    private final ServerConfig config;
    private MessageHandler handler;
    private final ExecutorService bossThreadPool;
    private final ExecutorService workThreadPool;
    private final List<ServerWorker> workers;


    public ServerImpl(ServerConfig config) throws IOException {
        this.config = config;
        this.workers = new LinkedList<>();
        this.bossThreadPool = Executors.newCachedThreadPool();
        this.workThreadPool = Executors.newCachedThreadPool();
    }


    @Override
    public void setHandler(MessageHandler handler) {
        this.handler = handler;
    }


    @Override
    public void start() throws IOException {
        for (Integer port : config.getTargetPort()) {
            ServerWorker worker = new ServerWorker();

            worker.setWorkThreadPool(workThreadPool);
            worker.setHandler(handler);
            worker.setPort(port);

            bossThreadPool.execute(worker);
            workers.add(worker);
        }
    }


    @Override
    public void close() throws IOException {
        for (ServerWorker worker : workers)
            worker.close();

        bossThreadPool.shutdownNow();
        workThreadPool.shutdownNow();
    }

}
